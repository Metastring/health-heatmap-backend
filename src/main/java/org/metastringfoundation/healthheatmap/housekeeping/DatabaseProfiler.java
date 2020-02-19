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

package org.metastringfoundation.healthheatmap.housekeeping;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.metastringfoundation.healthheatmap.helpers.Timer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;

public class DatabaseProfiler {
    private int datasets = 20;
    private int indicators = 500;
    private int entities = 500;

    long randomSeed;
    private Timer timer;

    private MongoCollection<Document> collectionOfDatasets;
    private MongoCollection<Document> collectionOfEntities;
    private MongoCollection<Document> skinnyCollection;

    Connection psqlConnection;
    Statement statement;

    String action;

    private String entityNameForQuery = "Entity100";
    private String datasetNameForQuery = "Dataset10";
    private String indicatorNameForQuery = "Indicator100";


    public DatabaseProfiler(String action) {
        timer = new Timer();
        this.action = action;
        randomSeed = 2048;

        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("healthdata");
        collectionOfDatasets = database.getCollection("groupedByDataset");
        collectionOfEntities = database.getCollection("groupedByEntities");
        skinnyCollection = database.getCollection("skinny");
        try {
            psqlConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/healthdata",
                "metastring", "metastring");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(1);
        }
    }

    public void run() {
        if (action.equals("fill")) {
            fillUpMongoDB();
            fillUpPostgresql();
        } else {
            queryPostgresql();
            queryMongoDB();
        }
    }

    private void fillUpMongoDB() {
        timer.reset();
        mongoAsSkinny();
        timer.result("Filling mongo as skinny tables");
    }

    private void fillUpPostgresql() {
        timer.reset();
        try {
            psqlAsDifferentTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        timer.result("Filling up postgresql as different tables");
    }

    private void dropMongo() {
        skinnyCollection.drop();
    }

    private void dropAndRecreatePSQL() throws SQLException {
        String dropDB = "DROP DATABASE healthdata;";
        Statement statement = psqlConnection.createStatement();
        statement.executeUpdate(dropDB);
    }

    private void mongoAsSkinny() {
        Random random = new Random(randomSeed);

        for (int datasetIndex = 0; datasetIndex < datasets; datasetIndex++) {
            for (int entityIndex = 0; entityIndex < entities; entityIndex++) {
                List<WriteModel<Document>> bulkList = new ArrayList<>();
                for (int indicatorIndex = 0; indicatorIndex < indicators ; indicatorIndex++) {
                    Document doc = new Document();
                    doc.append("value", random.nextFloat());
                    doc.append("entity", "Entity" + entityIndex);
                    doc.append("indicator", "Indicator" + indicatorIndex);
                    doc.append("dataset", "Dataset" + datasetIndex);
                    bulkList.add(new InsertOneModel<>(doc));
                }
                skinnyCollection.bulkWrite(bulkList);
            }
        }

        System.out.println("Filled MongoDB skinnyCollection");
        skinnyCollection.createIndex(Indexes.ascending("entity", "indicator", "dataset"));
        System.out.println("Created indexes for the collection");
    }

    private void psqlAsDifferentTables() throws SQLException {
        Random random = new Random(randomSeed);
        for (int datasetIndex = 0; datasetIndex < datasets; datasetIndex++) {
            String datasetName = "Dataset" + datasetIndex;
            Statement statement;
            statement = psqlConnection.createStatement();
            String tableCreationSQL = "CREATE TABLE " + datasetName + " " +
                    "(ENTITYNAME TEXT NOT NULL";
            for (int indicatorIndex = 0; indicatorIndex < indicators; indicatorIndex++) {
                String indicatorName = "Indicator" + indicatorIndex;
                tableCreationSQL += ", " + indicatorName + " NUMERIC";
            }
            tableCreationSQL += ");";
            statement.executeUpdate(tableCreationSQL);
            statement.close();

            psqlConnection.setAutoCommit(false);
            statement = psqlConnection.createStatement();
            for (int entityIndex = 0; entityIndex < entities; entityIndex++) {
                String entityName = "Entity" + entityIndex;
                String entityInsertionSQL = "INSERT INTO " + datasetName + " VALUES " +
                        "(" + wrapInQuote(entityName);
                for (int indicatorIndex = 0; indicatorIndex < indicators; indicatorIndex++) {
                    float value = random.nextFloat();
                    entityInsertionSQL += ", " + value;
                }
                entityInsertionSQL += ");";

                statement.executeUpdate(entityInsertionSQL);
            }
            statement.close();
            psqlConnection.commit();

            statement = psqlConnection.createStatement();
            for (int indicatorIndex = 0; indicatorIndex < indicators; indicatorIndex++) {
                String indicatorName = "Indicator" + indicatorIndex;
                String indexName = "idx_" + datasetName + "_" + indicatorName;
                StringBuilder createIndexSQL = new StringBuilder("CREATE INDEX ");
                createIndexSQL.append(indexName + " ON ");
                createIndexSQL.append(datasetName + "(" + indicatorName + ");");

                statement.executeUpdate(createIndexSQL.toString());
            }
            statement.close();
            psqlConnection.commit();
        }
    }

    public static String wrapInQuote(String word) {
        String quote = "'";
        return quote + word + quote;
    }

    private void queryMongoDB() {
        timer.reset();
        askMongoForSomeSpecificData();
        timer.result("mongoSpecificData");
        timer.reset();
        askMongoForAllDataOnAnEntity();
        timer.result("mongoFirstOfAllDataOnAnEntity");
        timer.reset();
        askMongoForHighestValueLessThanSomething((float) 0.5);
        timer.result("mongoHighestValueLessThanFloat");
    }
    private void queryPostgresql() {
        try {
            createPSQLStatement();
            timer.reset();
            askPsqlForSomeSpecificData();
            timer.result("psqlSpecificData");
            closePSQLStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void askMongoForSomeSpecificData() {
        Document result = skinnyCollection.find(and(
                eq("entity", entityNameForQuery),
                eq("dataset", datasetNameForQuery),
                eq("indicator", indicatorNameForQuery)
        )).first();

        System.out.println(result.get("value"));
    }

    private void askMongoForHighestValueLessThanSomething(float value) {
        Document result = skinnyCollection.find(lt("value", value))
                .sort(orderBy(descending("value"))).first();
        System.out.println(result.get("value"));
    }

    private void askMongoForAllDataOnAnEntity() {
        FindIterable<Document> documents = skinnyCollection.find(
                eq("entity", entityNameForQuery)
        );
        System.out.println(documents.first().get("value"));
    }

    private void createPSQLStatement() throws SQLException {
        statement = psqlConnection.createStatement();
    }

    private void askPsqlForSomeSpecificData() throws SQLException {

        String selectSQL;

        selectSQL = "Select " + indicatorNameForQuery + " from " +datasetNameForQuery +" WHERE entityname=" + wrapInQuote(entityNameForQuery) +";";

        ResultSet resultSet = statement.executeQuery(selectSQL);
        resultSet.next();
        float result = resultSet.getFloat(indicatorNameForQuery);
        System.out.println(result);
    }

    private void closePSQLStatement() throws SQLException {
        statement.close();
    }
}
