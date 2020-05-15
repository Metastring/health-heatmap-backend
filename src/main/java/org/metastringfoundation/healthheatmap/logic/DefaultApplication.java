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
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.healthheatmap.storage.DatasetStore;
import org.metastringfoundation.healthheatmap.storage.ElasticDatasetStore;
import org.metastringfoundation.healthheatmap.storage.ElasticManager;

import javax.inject.Inject;

/**
 * One (and only) implementation of the application that actually does the hard work of wiring everything together.
 * Brings everything else together to make web resources work, CLI, and anything else that needs to work.
 */
public class DefaultApplication implements Application {

    private static final Logger LOG = LogManager.getLogger(DefaultApplication.class);

    public final DatasetStore datasetStore;

    public static Application getDefaultDefaultApplication() {
        DatasetStore datasetStore = new ElasticManager();
        return new DefaultApplication(datasetStore);
    }

    @Inject
    public DefaultApplication(@ElasticDatasetStore DatasetStore datasetStore) {
        this.datasetStore = datasetStore;
    }

    @Override
    public void save(Dataset dataset) {
        datasetStore.save(dataset);
    }

    @Override
    public void shutdown() {

    }
}
