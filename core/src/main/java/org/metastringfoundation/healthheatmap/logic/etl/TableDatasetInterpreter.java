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

package org.metastringfoundation.healthheatmap.logic.etl;

import org.jboss.logging.Logger;
import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.TableToDatasetAdapter;
import org.metastringfoundation.datareader.dataset.table.csv.CSVTable;
import org.metastringfoundation.healthheatmap.beans.HealthDatasetBatchRead;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.HealthDatasetFromDataset;
import org.metastringfoundation.healthheatmap.helpers.HealthDatasetWithTransformsApplied;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;
import org.metastringfoundation.healthheatmap.logic.DatasetPointer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.metastringfoundation.healthheatmap.helpers.PathManager.guessMetadataPath;
import static org.metastringfoundation.healthheatmap.helpers.PathManager.guessRootMetadataPath;

/**
 * This is a utility that helps switch between table and dataset
 */
public class TableDatasetInterpreter {
    private static final Logger LOG = Logger.getLogger(TableDatasetInterpreter.class);

    /**
     * Independently get datasets from specified transformers
     *
     * @param tableAndDescriptionPair csv and its metadata
     * @param transformers            list of transformers
     * @return health dataset from the parameters (doesn't use system default transformers, etc)
     * @throws DatasetIntegrityError if there is an issue with the data
     */
    public static HealthDataset asHealthDataset(TableAndDescriptionPair tableAndDescriptionPair, List<DataTransformer> transformers) throws DatasetIntegrityError {
        Dataset dataset = new TableToDatasetAdapter(
                tableAndDescriptionPair.getTable(),
                tableAndDescriptionPair.getTableDescription()
        );
        return new HealthDatasetFromDataset(dataset, transformers);
    }

    public static HealthDataset asHealthDataset(Dataset dataset, List<DataTransformer> transformers) {
        return HealthDatasetWithTransformsApplied.from(new HealthDatasetFromDataset(dataset, transformers));
    }

    public static HealthDatasetBatchRead readHealthDatasetBatch(List<DatasetPointer> datasetPointers) {
        List<HealthDataset> result = new ArrayList<>();
        Map<String, Exception> errors = new HashMap<>();
        for (DatasetPointer datasetPointer: datasetPointers) {
            LOG.info("Interpreting file: " + datasetPointer.getName());
            try {
                result.add(asHealthDataset(datasetPointer));
            } catch (DatasetIntegrityError | IOException exception) {
                errors.put(datasetPointer.getName(), exception);
            }
        }
        return new HealthDatasetBatchRead(result, errors);
    }

    private static HealthDataset asHealthDataset(DatasetPointer datasetPointer) throws IOException, DatasetIntegrityError {
        HealthDataset dataset = asHealthDataset(datasetPointer.getDataset(), datasetPointer.getTransformers());
        return addSourceName(dataset, datasetPointer.getName());
    }

    private static HealthDataset addSourceName(HealthDataset dataset, String name) {
        dataset.getData().forEach(d -> d.put("meta.datafile", name));
        return dataset;
    }

    public static void print(List<Path> dataFiles) throws IOException, DatasetIntegrityError {
        for (Path file : dataFiles) {
            LOG.info("File: " + file.toString());
            printEachDataPoint(file, getMetadataFiles(file));
            System.out.println("\n\n\n");
        }
    }

    @Nonnull
    private static List<Path> getMetadataFiles(Path file) {
        return List.of(
                guessRootMetadataPath(file),
                guessMetadataPath(file)
        );
    }

    public static void printConcise(List<Path> paths) throws IOException, DatasetIntegrityError {
        for (Path path : paths) {
            printUniqueDimensionValuesOf(path, getMetadataFiles(path));
        }
    }

    private static void printUniqueDimensionValuesOf(Path path, List<Path> metadataFiles) throws IOException, DatasetIntegrityError {
        Map<String, Set<String>> dimensionValues = getUniqueDimensionValuesOf(path, metadataFiles);
        dimensionValues.forEach((key, value) -> {
            System.out.println(key);
            value.forEach(System.out::println);
            System.out.println("\n");
        });
    }

    private static Map<String, Set<String>> getUniqueDimensionValuesOf(Path path, List<Path> metadataFiles) throws IOException, DatasetIntegrityError {
        Dataset dataset = getDataset(path, metadataFiles);
        return dataset.getData().stream()
                .map(DataPoint::getAsMap)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet()))
                );
    }

    private static void printEachDataPoint(Path path, List<Path> metadataFiles) throws IOException, DatasetIntegrityError {
        Dataset dataset = getDataset(path, metadataFiles);
        System.out.println(path);
        for (DataPoint dataPoint : dataset.getData()) {
            System.out.println(dataPoint);
        }
    }

    private static Dataset getDataset(Path path, List<Path> metadataFiles) throws DatasetIntegrityError, IOException {
        TableAndDescriptionPair tableAndDescription = new TableAndDescriptionPair(new CSVTable(path), metadataFiles);
        return new TableToDatasetAdapter(
                tableAndDescription.getTable(),
                tableAndDescription.getTableDescription()
        );
    }

    public static void printTransformersReport(Collection<DataTransformer> transformers) {
        if (transformers != null) {
            transformers.forEach(TableDatasetInterpreter::printTransformerReport);
        }
    }

    private static void printTransformerReport(DataTransformer transformer) {
        Set<Map<String, String>> keyFailure = transformer.getUnmatchedKeysFound();
        String csv = tryConvert(keyFailure);
        LOG.info(csv);
    }

    private static String tryConvert(Set<Map<String, String>> keyFailure) {
        List<Map<String, String>> keyFailureRecords = new ArrayList<>(keyFailure);
        if (keyFailureRecords.size() < 1) {
            return "NO KEY FAILURES";
        }
        try {
            return KeyValuePairsToCSV.convertToCSVWithFirstElementKeysAsHeaders(keyFailureRecords);
        } catch (IOException e) {
            e.printStackTrace();
            return "COULD NOT GENERATE CSV";
        }
    }
}
