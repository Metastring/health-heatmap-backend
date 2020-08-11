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

package org.metastringfoundation.healthheatmap.logic;

import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.TableToDatasetAdapter;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.HealthDatasetFromDataset;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;
import org.metastringfoundation.healthheatmap.storage.ApplicationMetadataStore;
import org.metastringfoundation.healthheatmap.storage.DatasetStore;
import org.metastringfoundation.healthheatmap.storage.ElasticManager;
import org.metastringfoundation.healthheatmap.storage.ElasticStore;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.beans.DownloadRequest;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * One (and only) implementation of the application that actually does the hard work of wiring everything together.
 * Brings everything else together to make web resources work, CLI, and anything else that needs to work.
 */
@ApplicationScoped
public class ApplicationDefault implements Application {
    private final DatasetStore datasetStore;
    private final ApplicationMetadataStore metadataStore;

    public ApplicationDefault(@ElasticStore DatasetStore datasetStore) {
        this(datasetStore, (ApplicationMetadataStore) datasetStore);
    }

    @Inject
    public ApplicationDefault(@ElasticStore DatasetStore datasetStore, @ElasticStore ApplicationMetadataStore metadataStore) {
        this.datasetStore = datasetStore;
        this.metadataStore = metadataStore;
    }

    public static Application createPreconfiguredApplicationDefault() {
        DatasetStore datasetStore = new ElasticManager();
        return new ApplicationDefault(datasetStore);
    }

    @Override
    public void save(HealthDataset dataset) throws IOException {
        datasetStore.save(dataset);
    }

    @Override
    public DataQueryResult query(DataQuery dataQuery) throws IOException {
        return datasetStore.query(dataQuery);
    }

    @Override
    public List<Map<String, Object>> getAllTermsOfFields(FilterAndSelectFields filterAndFields) throws IOException {
        return datasetStore.getAllTermsOfFields(filterAndFields);
    }

    @Override
    public void factoryReset() throws IOException {
        datasetStore.factoryReset();
    }

    @Override
    public void shutdown() throws IOException {
        datasetStore.shutdown();
    }

    @Override
    public HealthDataset asHealthDataset(TableAndDescriptionPair tableAndDescriptionPair, List<DataTransformer> transformers) throws DatasetIntegrityError {
        Dataset dataset = new TableToDatasetAdapter(
                tableAndDescriptionPair.getTable(),
                tableAndDescriptionPair.getTableDescription()
        );
        return new HealthDatasetFromDataset(dataset, transformers);
    }

    @Override
    public void logDownload(DownloadRequest downloadRequest) throws IOException {
        metadataStore.logDownload(downloadRequest);
    }

    @Override
    public boolean getHealth() throws IOException {
        return datasetStore.getHealth();
    }
}
