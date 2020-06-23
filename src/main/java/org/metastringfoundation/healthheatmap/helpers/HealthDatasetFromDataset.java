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
import org.metastringfoundation.healthheatmap.logic.DataTransformer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HealthDatasetFromDataset implements HealthDataset {
    private final Dataset dataset;
    private List<DataTransformer> dataTransformers;

    public HealthDatasetFromDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public HealthDatasetFromDataset(Dataset dataset, List<DataTransformer> transformers) {
        this.dataset = dataset;
        setTransformers(transformers);
    }

    public void setTransformers(List<DataTransformer> transformers) {
        dataTransformers = transformers;
    }

    @Override
    public Collection<? extends Map<String, String>> getData() {
        if (dataTransformers == null) {
            return dataset.getData();
        } else {
            return dataAfterTransforms();
        }
    }

    private Collection<? extends Map<String, String>> dataAfterTransforms() {
        return dataset.getData().stream()
                .map(this::applyTransform)
                .collect(Collectors.toList());
    }

    private <T extends Map<String, String>> T applyTransform(T data) {
        for (DataTransformer dataTransformer : dataTransformers) {
            data = dataTransformer.transform(data);
        }
        return data;
    }
}
