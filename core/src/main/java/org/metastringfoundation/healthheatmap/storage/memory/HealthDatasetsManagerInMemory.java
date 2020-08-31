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

package org.metastringfoundation.healthheatmap.storage.memory;

import org.jboss.logging.Logger;
import org.metastringfoundation.healthheatmap.beans.HealthDatasetBatchRead;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.logic.DatafilesManager;
import org.metastringfoundation.healthheatmap.logic.DatasetPointer;
import org.metastringfoundation.healthheatmap.logic.DimensionsManager;
import org.metastringfoundation.healthheatmap.logic.HealthDatasetsManager;
import org.metastringfoundation.healthheatmap.logic.etl.TableDatasetInterpreter;
import org.metastringfoundation.healthheatmap.storage.beans.ValuePointAssociation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class HealthDatasetsManagerInMemory implements HealthDatasetsManager {
    private static final Logger LOG = Logger.getLogger(HealthDatasetsManager.class);
    private final DatafilesManager datafilesManager;
    private final DimensionsManager dimensionsManager;
    private final Map<String, HealthDataset> readDatasetsCache = new LinkedHashMap<>();
    private final Set<ValuePointAssociation> valuePointAssociations = new HashSet<>();

    @Inject
    public HealthDatasetsManagerInMemory(DatafilesManager datafilesManager, DimensionsManager dimensionsManager) {
        this.datafilesManager = datafilesManager;
        this.dimensionsManager = dimensionsManager;
    }

    public void loadAllDatasets() {
        List<DatasetPointer> datasetPointers = datafilesManager.getAllDatasets();
        loadDatasetsToCacheFromPointers(datasetPointers);
    }

    @Override
    public Map<String, HealthDataset> getDatasetsUnderNameWithAugmentation(String name) {
        List<String> dataFilesAtPath = datafilesManager.getDatasetsAtName(name);
        return getsDatasetsWithAugmentationFromList(dataFilesAtPath);
    }

    @Override
    public Map<String, HealthDataset> getDatasetsWithAugmentation(List<String> datasetNames) {
        return getsDatasetsWithAugmentationFromList(datasetNames);
    }

    private Map<String, HealthDataset> getsDatasetsWithAugmentationFromList(List<String> datasetNames) {
        loadToCacheIfNecessary(datasetNames);
        return readDatasetsCache.entrySet().stream()
                .filter(entry -> datasetNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void loadToCacheIfNecessary(List<String> dataFilesAtPath) {
        List<String> mustLoad = dataFilesAtPath.stream()
                .filter(name -> !readDatasetsCache.containsKey(name))
                .collect(Collectors.toList());
        loadDatasetsToCache(mustLoad);
    }

    private void loadDatasetsToCache(List<String> mustLoad) {
        List<DatasetPointer> pointers = datafilesManager.getTheseDatasets(mustLoad);
        loadDatasetsToCacheFromPointers(pointers);
    }

    private void loadDatasetsToCacheFromPointers(List<DatasetPointer> pointers) {
        HealthDatasetBatchRead healthDatasetsRead = TableDatasetInterpreter.readHealthDatasetBatch(pointers);
        healthDatasetsRead.getErrors().forEach((file, error) -> LOG.error(file + " could not be read: " + error.getMessage()));
        for (Map.Entry<String, HealthDataset> entry : healthDatasetsRead.getDatasets().entrySet()) {
            HealthDataset augmented = dimensionsManager.augmentDatasetWithDimensionInfo(entry.getValue());
            readDatasetsCache.put(entry.getKey(), augmented);
        }
        for (Map.Entry<String, HealthDataset> entry : healthDatasetsRead.getDatasets().entrySet()) {
            List<ValuePointAssociation> associations = dimensionsManager.getDimensionAssociationsOf(entry.getValue().getData());
            valuePointAssociations.addAll(associations);
        }
    }

    public Map<String, List<String>> getAssociationsOf(String dimension, String id) {
        return valuePointAssociations.stream()
                .filter(point -> id.equals(point.dynamicDimensions.get(dimension)))
                .flatMap(vp -> vp.dynamicDimensions.entrySet().stream())
                .distinct()
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
}
