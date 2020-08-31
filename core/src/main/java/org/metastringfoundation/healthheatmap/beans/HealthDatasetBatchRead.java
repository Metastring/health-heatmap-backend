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

package org.metastringfoundation.healthheatmap.beans;

import org.metastringfoundation.healthheatmap.helpers.HealthDataset;

import java.util.Map;

public class HealthDatasetBatchRead {
    private final Map<String, HealthDataset> datasets;
    private final Map<String, Exception> errors;

    public HealthDatasetBatchRead(Map<String, HealthDataset> datasets, Map<String, Exception> errors) {
        this.datasets = datasets;
        this.errors = errors;
    }

    public Map<String, HealthDataset> getDatasets() {
        return datasets;
    }

    public Map<String, Exception> getErrors() {
        return errors;
    }
}
