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
import org.metastringfoundation.healthheatmap.helpers.FileManager;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.HealthDatasetSimple;
import org.metastringfoundation.healthheatmap.helpers.ReadCSVAsMap;
import org.metastringfoundation.healthheatmap.logic.DimensionsManager;
import org.metastringfoundation.healthheatmap.logic.FileStore;
import org.metastringfoundation.healthheatmap.storage.beans.ValuePointAssociation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.*;

@ApplicationScoped
public class DimensionsManagerInMemory implements DimensionsManager {
    private static final Logger LOG = Logger.getLogger(DimensionsManagerInMemory.class);
    private final FileStore fileStore;
    //         dimension    id          param   value
    private Map<String, Map<String, Map<String, String>>> backingMap;
    // eg:     entity       KL_KN       district kannur

    //          indicator    dimension    param  value
    private final Map<String, Map<String, Map<String, String>>> indicatorAssociations = new HashMap<>();

    @Inject
    public DimensionsManagerInMemory(FileStore fileStore) throws IOException {
        this.fileStore = fileStore;
        this.backingMap = calculateDimensions();
        LOG.info("Loaded " + backingMap.keySet().size() + " dimensions");
    }

    private Map<String, Map<String, Map<String, String>>> calculateDimensions() throws IOException {
        Map<String, Map<String, Map<String, String>>> allDimensions = new LinkedHashMap<>();
        List<Path> files = fileStore.getFiles(fileStore.getDimensionsDirectory());
        for (Path file : files) {
            String nameWithExtension = fileStore.getRelativeName(file, fileStore.getDimensionsDirectory());
            String name = FileManager.dropExtension(nameWithExtension);
            Map<String, Map<String, String>> currentDimension = new LinkedHashMap<>();
            allDimensions.put(name, currentDimension);
            ReadCSVAsMap.get(file).forEach(rec -> {
                String id = rec.get("id");
                currentDimension.put(id, rec);
            });
        }
        return allDimensions;
    }

    @Override
    public void refresh() throws IOException {
        this.backingMap = calculateDimensions();
    }

    @Override
    public Set<String> getValidIdsOf(String dimension) {
        return backingMap.get(dimension).keySet();
    }

    @Override
    public Boolean idExists(String dimension, String id) {
        return idExists(backingMap.get(dimension), id);
    }

    private Boolean idExists(Map<String, Map<String, String>> dimensionMap, String id) {
        return dimensionMap.containsKey(id);
    }

    @Override
    public Boolean dimensionExists(String dimension) {
        return backingMap.containsKey(dimension);
    }

    @Override
    public void createDimension(String dimension) {
        if (dimensionExists(dimension)) {
            throw new IllegalArgumentException("Dimension " + dimension + " already exists");
        } else {
            backingMap.put(dimension, new LinkedHashMap<>());
        }
    }

    @Override
    public void addValuesToDimension(String dimension, Map<String, Map<String, String>> values) {
        Map<String, Map<String, String>> thisDimension = backingMap.get(dimension);
        values.keySet().forEach(id -> {
            if (idExists(thisDimension, id)) {
                throw new IllegalArgumentException("ID " + id + " already exists in dimension " + dimension);
            }
        });
        thisDimension.putAll(values);
    }

    @Override
    public List<HealthDataset> augmentDatasetsWithDimensionInfo(Collection<HealthDataset> datasets) {
        return datasets.stream()
                .map(this::augmentDatasetWithDimensionInfo)
                .collect(toList());
    }

    @Override
    public HealthDataset augmentDatasetWithDimensionInfo(HealthDataset dataset) {
        return new HealthDatasetSimple(
                dataset.getData().stream()
                        .map(this::augmentDataPointWithDimensionInfo)
                        .collect(toList()),
                dataset.getDataPointsWithError());
    }

    private Map<String, String> augmentDataPointWithDimensionInfo(Map<String, String> datapoint) {
        Map<String, String> augmentedPoint = new LinkedHashMap<>();
        datapoint.forEach((dimension, value) -> {
            if (dimension.startsWith("meta.")) {
                augmentedPoint.put(dimension, value);
            } else {
                if (dimension.contains(".")) {
                    augmentedPoint.put("meta.transformed." + dimension, value);
                } else {
                    augmentedPoint.put("meta.transformed." + dimension + ".id", value);
                }
                if (dimensionExists(dimension)) {
                    augmentWithExistingDimensionInfo(augmentedPoint, dimension, value);
                } else if (dimensionAllowed(dimension)) {
                    augmentedPoint.put(dimension, value);
                }
            }
        });
        return augmentedPoint;
    }

    private void augmentWithExistingDimensionInfo(Map<String, String> augmentedPoint, String dimension, String value) {
        if (!value.equals("NULL")) {
            getDimensionRecordIfExists(dimension, value).ifPresentOrElse(
                    record -> record.forEach((prop, propValue) -> augmentedPoint.put(dimension + "." + prop, propValue)),
                    () -> augmentedPoint.put(dimension + ".id", "UNKNOWN")
            );
        } // else, which means the dimension id is "NULL", we just drop the dimension because the dimension shouldn't exist
        // and its existence is an anomaly of data representation in upstream
    }

    private Boolean dimensionAllowed(String dimension) {
        return dimension.equals("value") || dimension.startsWith("duration.");
    }

    private Optional<Map<String, String>> getDimensionRecordIfExists(String dimension, String id) {
        if (dimensionExists(dimension) && idExists(dimension, id)) {
            return Optional.of(backingMap.get(dimension).get(id));
        } else {
            return Optional.empty();
        }
    }

    // TODO: Use a graph database here
    @Override
    public void persistAssociationWithIndicator(Collection<HealthDataset> datasets) {
        for (HealthDataset dataset : datasets) {
            dataset.getData().forEach(point -> {
                if (point.containsKey("indicator.id")) {
                    String indicator = point.get("indicator.id");
                    point.forEach((key, value) -> {
                        if (!key.startsWith("meta.") && key.endsWith(".id")) {
                            String dimension = key.split("\\.")[0];
                            if (dimensionExists(dimension)) {
                                getDimensionRecordIfExists(dimension, value).ifPresent(
                                        linkedMap -> indicatorAssociations.computeIfAbsent(indicator, whatever -> new HashMap<>()).put(dimension, linkedMap)
                                );
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * Takes something like this:
     * indicator    dimension    param  value
     * private Map<String, Map<String, Map<String, String>>> indicatorAssociations;
     * <p>
     * and returns
     * dimension    id, id, id
     *
     * @param indicator the id which needs to be queries
     * @return list of fields with values
     */

    @Override
    public Map<String, List<String>> fieldsAssociatedWithIndicator(String indicator) {
        return indicatorAssociations.get(indicator).entrySet().stream()
                .collect(groupingBy(Map.Entry::getKey, mapping(entry -> entry.getValue().get("id"), toList())));
    }

    @Override
    public List<String> getAllIndicatorsWithAssociations() {
        return new ArrayList<>(indicatorAssociations.keySet());
    }

    @Override
    public List<ValuePointAssociation> getDimensionAssociationsOf(List<? extends Map<String, String>> data) {
        List<ValuePointAssociation> result = new ArrayList<>();
        data.forEach(datapoint -> {
            Map<String, String> staticValues = new HashMap<>();
            Map<String, String> dynamicValues = new HashMap<>();
            datapoint.forEach((dimension, value) -> {
                if (dimensionAllowed(dimension)) {
                    staticValues.put(dimension, value);
                } else if (dimension.equals("meta.datafile")) {
                    staticValues.put(dimension, value);
                } else if (dimensionExists(dimension) && idExists(dimension, value)) {
                    dynamicValues.put(dimension, value);
                }
            });
            result.add(ValuePointAssociation.of(staticValues, dynamicValues));
        });
        return result;
    }

    @Override
    public List<String> getKnownDimensions() {
        return new ArrayList<>(backingMap.keySet());
    }
}
