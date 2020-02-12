/*
 *    Copyright 2020 Metastring Foundation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.metastringfoundation.healthheatmap;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public class MongoDB implements Database {
    private MongoCollection<Document> collectionOfDatasets;
    private MongoCollection<Document> collectionOfEntities;
    private MongoCollection<Document> skinnyCollection;

    public MongoDB() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("healthdata");
        this.collectionOfDatasets = database.getCollection("groupedByDataset");
        this.collectionOfEntities = database.getCollection("groupedByEntities");
        this.skinnyCollection = database.getCollection("skinny");
    }

    @Override
    public void addDataset(Dataset dataset) {
        addDatasetAsDatasets(dataset);
        addDatasetAsEntities(dataset);
        addDatasetAsSkinny(dataset);
    }

    private void addDatasetAsDatasets(Dataset dataset) {
        List<String> indicators = dataset.getIndicators();
        List<String> entities = dataset.getEntities();
        List<List<DataPoint>> dataGroupedByEntities = dataset.getDataGroupedByEntities();
        DatasetMetadata metadata = dataset.getMetadata();

        String datasetName = metadata.getName();


        Bson datasetFilter = eq("_metadata.name", datasetName);
        List<Bson> datasetUpdates = new ArrayList<>();

        for (int entityIndex = 0; entityIndex < entities.size(); entityIndex++) {
            String entityName = entities.get(entityIndex);

            for (int indicatorIndex = 0; indicatorIndex < indicators.size(); indicatorIndex++) {
                String indicatorName = indicators.get(indicatorIndex);
                String fieldToUpdate = entityName + "." + indicatorName;
                Object value = dataGroupedByEntities.get(entityIndex).get(indicatorIndex).getValue();
                datasetUpdates.add(set(fieldToUpdate, value));
            }
        }

        Bson datasetUpdate = combine(datasetUpdates);

        UpdateOptions upsert = new UpdateOptions().upsert(true);
        this.collectionOfDatasets.updateOne(
                datasetFilter,
                datasetUpdate,
                upsert
        );
    }

    private void addDatasetAsEntities(Dataset dataset) {
        List<String> indicators = dataset.getIndicators();
        List<String> entities = dataset.getEntities();
        List<List<DataPoint>> dataGroupedByEntities = dataset.getDataGroupedByEntities();
        DatasetMetadata metadata = dataset.getMetadata();

        String datasetName = metadata.getName();

        for (int entityIndex = 0; entityIndex < entities.size(); entityIndex++) {
            String entityName = entities.get(entityIndex);
            Bson datasetFilter = eq("name", entityName);

            List<Bson> datasetUpdates = new ArrayList<>();

            for (int indicatorIndex = 0; indicatorIndex < indicators.size(); indicatorIndex++) {
                String indicatorName = indicators.get(indicatorIndex);
                String fieldToUpdate = indicatorName;
                Object value = dataGroupedByEntities.get(entityIndex).get(indicatorIndex).getValue();
                Document dataDoc = new Document();
                dataDoc.append("value", value);
                dataDoc.append("source", datasetName);
                datasetUpdates.add(addToSet(fieldToUpdate, dataDoc));
            }
            Bson datasetUpdate = combine(datasetUpdates);

            UpdateOptions upsert = new UpdateOptions().upsert(true);
            this.collectionOfEntities.updateOne(
                    datasetFilter,
                    datasetUpdate,
                    upsert
            );
        }
    }

    private void addDatasetAsSkinny(Dataset dataset) {
        List<String> indicators = dataset.getIndicators();
        List<String> entities = dataset.getEntities();
        List<List<DataPoint>> dataGroupedByEntities = dataset.getDataGroupedByEntities();
        DatasetMetadata metadata = dataset.getMetadata();
        UpdateOptions upsert = new UpdateOptions().upsert(true);

        String datasetName = metadata.getName();
        String entityName;

        class MongoSave implements Runnable {
            private int entityIndex;

            private MongoSave(
                    int entityIndex
            ) {
                this.entityIndex = entityIndex;
            }
            public void run() {
                String entityName = entities.get(entityIndex);
                List<DataPoint> dataOfTheEntity = dataGroupedByEntities.get(entityIndex);
                for (int indicatorIndex = 0; indicatorIndex < indicators.size(); indicatorIndex++) {
                    String indicatorName = indicators.get(indicatorIndex);

                    Bson datasetFilter = and(
                            eq("entity", entityName),
                            eq("indicator", indicatorName),
                            eq("source", datasetName)
                    );

                    Object value = dataOfTheEntity.get(indicatorIndex).getValue();
                    Bson update = set("value", value);
                    skinnyCollection.updateOne(
                            datasetFilter,
                            update,
                            upsert
                    );
                }
            }
        }

        for (int entityIndex = 0; entityIndex < entities.size(); entityIndex++) {
            Thread t = new Thread(new MongoSave(entityIndex));
            t.start();
        }
    }
}