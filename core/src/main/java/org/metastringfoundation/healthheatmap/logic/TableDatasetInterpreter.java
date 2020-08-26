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

import org.jboss.logging.Logger;
import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.TableToDatasetAdapter;
import org.metastringfoundation.healthheatmap.beans.HealthDatasetBatchRead;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.HealthDatasetFromDataset;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is a utility that helps switch between table and dataset
 */
public class TableDatasetInterpreter {
    private static final Logger LOG = Logger.getLogger(TableDatasetInterpreter.class);

    private final List<DataTransformer> transformers;

    @Nonnull
    public static TableDatasetInterpreter getTableDatasetInterpreterWithTransformersIfAvailable(Path transformersDirectory) throws IOException {
        TableDatasetInterpreter tableDatasetInterpreter;
        if (transformersDirectory != null && Files.isDirectory(transformersDirectory) && Files.isReadable(transformersDirectory)) {
            LOG.info("Reading transformers from: " + transformersDirectory.toString());
            List<DataTransformer> transformers = Stream.of(
                    List.of(new DataTransformerForEntityType()),
                    DataTransformersReader.getFromPath(transformersDirectory).getTransformers(),
                    List.of(new DataTransformerForDates())
            ).flatMap(Collection::stream)
                    .collect(Collectors.toList());
            tableDatasetInterpreter = new TableDatasetInterpreter(
                    transformers
            );
        } else {
            LOG.info("Initializing tableDatasetInterpreter without transformers. Make sure you mean this.");
            tableDatasetInterpreter = new TableDatasetInterpreter();
        }
        return tableDatasetInterpreter;
    }

    public TableDatasetInterpreter() {
        this.transformers = List.of();
    }

    public TableDatasetInterpreter(List<DataTransformer> transformers) {
        this.transformers = transformers;
    }

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

    public HealthDatasetBatchRead getAsDatasets(List<Path> dataFiles) {
        List<HealthDataset> result = new ArrayList<>();
        Map<Path, Exception> errors = new HashMap<>();
        for (Path dataFile : dataFiles) {
            LOG.info("Interpreting file: " + dataFile.toString());
            try {
                result.add(getAsDatasetsFromFile(dataFile));
            } catch (DatasetIntegrityError | IOException exception) {
                errors.put(dataFile, exception);
            }
        }
        return new HealthDatasetBatchRead(result, errors);
    }

    private HealthDataset getAsDatasetsFromFile(Path path) throws IOException, DatasetIntegrityError {
        TableAndDescriptionPair tableAndDescriptionPair = new TableAndDescriptionPair(path);
        return asHealthDataset(tableAndDescriptionPair, transformers);
    }

    public void print(List<Path> dataFiles) throws IOException, DatasetIntegrityError {
        for (Path file : dataFiles) {
            LOG.info("File: " + file.toString());
            printEachDataPoint(file);
            System.out.println("\n\n\n");
        }
    }

    public void printConcise(List<Path> paths) throws IOException, DatasetIntegrityError {
        for (Path path : paths) {
            printUniqueDimensionValuesOf(path);
        }
    }

    private void printUniqueDimensionValuesOf(Path path) throws IOException, DatasetIntegrityError {
        Map<String, Set<String>> dimensionValues = getUniqueDimensionValuesOf(path);
        dimensionValues.forEach((key, value) -> {
            System.out.println(key);
            value.forEach(System.out::println);
            System.out.println("\n");
        });
    }

    private Map<String, Set<String>> getUniqueDimensionValuesOf(Path path) throws IOException, DatasetIntegrityError {
        Dataset dataset = getDataset(path);
        return dataset.getData().stream()
                .map(DataPoint::getAsMap)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet()))
                );
    }

    private void printEachDataPoint(Path path) throws IOException, DatasetIntegrityError {
        Dataset dataset = getDataset(path);
        System.out.println(path);
        for (DataPoint dataPoint : dataset.getData()) {
            System.out.println(dataPoint);
        }
    }

    private Dataset getDataset(Path path) throws DatasetIntegrityError, IOException {
        TableAndDescriptionPair tableAndDescription = new TableAndDescriptionPair(path);
        return new TableToDatasetAdapter(
                tableAndDescription.getTable(),
                tableAndDescription.getTableDescription()
        );
    }

    public void printTransformersReport() {
        if (transformers != null) {
            transformers.forEach(this::printTransformerReport);
        }
    }

    private void printTransformerReport(DataTransformer transformer) {
        Set<Map<String, String>> keyFailure = transformer.getUnmatchedKeysFound();
        String csv = tryConvert(keyFailure);
        LOG.info(csv);
    }

    private String tryConvert(Set<Map<String, String>> keyFailure) {
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
