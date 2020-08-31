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

import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class HealthDatasetFromDataset implements HealthDataset {
    private final Dataset dataset;
    private final List<DataTransformer> dataTransformers;
    private final Map<DataPoint, Map<String, String>> errors = new HashMap<>();

    public HealthDatasetFromDataset(Dataset dataset) {
        this.dataset = dataset;
        dataTransformers = List.of();
    }

    public HealthDatasetFromDataset(Dataset dataset, @Nullable List<DataTransformer> transformers) {
        this.dataset = dataset;
        dataTransformers = Objects.requireNonNullElseGet(transformers, List::of);
    }

    @Override
    public List<? extends Map<String, String>> getData() {
        if (dataTransformers == null || dataTransformers.size() == 0) {
            return dataset.getData();
        } else {
            return dataAfterTransforms();
        }
    }

    @Override
    public Map<DataPoint, Map<String, String>> getDataPointsWithError() {
        return errors;
    }

    private List<? extends Map<String, String>> dataAfterTransforms() {
        return dataset.getData().stream()
                .map(this::storeOriginalInMetaField)
                .map(this::applyTransform)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private DataPoint storeOriginalInMetaField(DataPoint dataPoint) {
        DataPoint dataPointWithOriginal = DataPoint.from(dataPoint);
        dataPointWithOriginal.putAll(
                dataPoint.entrySet().stream()
                        .collect(Collectors.toMap(e -> "meta.original." + e.getKey(), Map.Entry::getValue))
        );
        return dataPointWithOriginal;
    }

    private List<DataPoint> applyTransform(DataPoint data) {
        List<DataPoint> expandingList = List.of(data);
        for (DataTransformer dataTransformer : dataTransformers) {
            List<DataPoint> list = new ArrayList<>();
            for (DataPoint dataPoint : expandingList) {
                try {
                    List<DataPoint> transform = dataTransformer.transform(dataPoint);
                    list.addAll(transform);
                } catch (UnknownValueException e) {
                    list.add(dataPoint);
                    Map<String, String> erringFields = dataTransformer.getKeyApplicable(dataPoint);
                    errors.put(dataPoint, erringFields);
                }

            }
            expandingList = list;
        }
        return expandingList;
    }
}
