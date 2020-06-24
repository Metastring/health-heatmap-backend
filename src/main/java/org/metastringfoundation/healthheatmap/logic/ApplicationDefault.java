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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.storage.DatasetStore;
import org.metastringfoundation.healthheatmap.storage.ElasticManager;
import org.metastringfoundation.healthheatmap.storage.ElasticStore;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * One (and only) implementation of the application that actually does the hard work of wiring everything together.
 * Brings everything else together to make web resources work, CLI, and anything else that needs to work.
 */
public class ApplicationDefault implements Application {
    private static final Logger LOG = LogManager.getLogger(ApplicationDefault.class);
    public final DatasetStore datasetStore;

    public static Application createPreconfiguredApplicationDefault() {
        DatasetStore datasetStore = new ElasticManager();
        return new ApplicationDefault(datasetStore);
    }

    @Inject
    public ApplicationDefault(@ElasticStore DatasetStore datasetStore) {
        this.datasetStore = datasetStore;
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
    public List<Map<String, Object>> getAllTermsOfFields(List<String> fields) throws IOException {
        return datasetStore.getAllTermsOfFields(fields);
    }

    @Override
    public void factoryReset() throws IOException {
        datasetStore.factoryReset();
    }

    @Override
    public void shutdown() throws IOException {
        datasetStore.shutdown();
    }
}
