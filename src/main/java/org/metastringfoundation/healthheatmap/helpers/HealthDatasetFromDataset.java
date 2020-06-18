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

package org.metastringfoundation.healthheatmap.helpers;

import org.metastringfoundation.data.Dataset;

import java.util.Collection;
import java.util.Map;

public class HealthDatasetFromDataset implements HealthDataset {
    private final Dataset dataset;

    public HealthDatasetFromDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public Collection<? extends Map<String, String>> getData() {
        return dataset.getData();
    }
}
