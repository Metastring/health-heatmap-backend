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

import java.util.List;
import java.util.Map;

public class HealthDatasetSimple implements HealthDataset {
    private final Map<DataPoint, Map<String, String>> dataPointsWithError;
    private List<? extends Map<String, String>> dataPoints;

    public HealthDatasetSimple(List<? extends Map<String, String>> dataPoints, Map<DataPoint, Map<String, String>> dataPointsWithError) {
        this.dataPoints = dataPoints;
        this.dataPointsWithError = dataPointsWithError;
    }

    public static HealthDatasetSimple from(HealthDatasetFromDataset preTransformDataset) {
        return new HealthDatasetSimple(preTransformDataset.getData(), preTransformDataset.getDataPointsWithError());
    }

    @Override
    public List<? extends Map<String, String>> getData() {
        return dataPoints;
    }

    @Override
    public Map<DataPoint, Map<String, String>> getDataPointsWithError() {
        return dataPointsWithError;
    }

    public void setData(List<? extends Map<String, String>> dataPoints) {
        this.dataPoints = dataPoints;
    }
}
