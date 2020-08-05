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

package org.metastringfoundation.healthheatmap.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.TableToDatasetAdapter;
import org.metastringfoundation.healthheatmap.helpers.FileManager;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;
import org.metastringfoundation.healthheatmap.logic.KeyValuePairsToCSV;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is a utility that helps upload data directly from command line
 */
public class TableUploader {
    private static final Logger LOG = LogManager.getLogger(TableUploader.class);

    private final Application application;
    private final List<DataTransformer> transformers;

    @Inject
    public TableUploader(Application application) {
        this.application = application;
        this.transformers = List.of();
    }

    public TableUploader(Application application, List<DataTransformer> transformers) {
        this.application = application;
        this.transformers = transformers;
    }

    /**
     * Uploads the data into the database of the application.
     *
     * @param path - path to the CSV file that contains data
     */
    public void upload(String path) throws IOException, DatasetIntegrityError {
        TableAndDescriptionPair tableAndDescription = new TableAndDescriptionPair(path);
        HealthDataset healthDataset = application.asHealthDataset(tableAndDescription, transformers);
        application.save(healthDataset);
        LOG.info("Done persisting dataset");
    }

    public void print(String path) throws IOException, DatasetIntegrityError {
        if (isSingleRegularFile(path)) {
            printSingle(path);
        } else {
            printMultiple(path);
        }

    }

    private void printMultiple(String path) throws IOException {
        Collection<Path> dataFiles = FileManager.getDataFilesInDirectory(Paths.get(path));
        dataFiles.stream()
                .peek(file -> LOG.info("File: " + file.toString()))
                .forEach(file -> {
                    try {
                        printUniqueDimensionValuesOf(file.toString());
                        System.out.println("\n\n\n");
                    } catch (IOException | DatasetIntegrityError e) {
                        e.printStackTrace();
                    }
                });
    }

    private void printAllDataPointsOf(String path) throws IOException, DatasetIntegrityError {
        Dataset dataset = getDataset(path);
        dataset.getData()
                .forEach(System.out::println);
    }

    private void printUniqueDimensionValuesOf(String path) throws IOException, DatasetIntegrityError {
        Map<String, Set<String>> dimensionValues = getUniqueDimensionValuesOf(path);
        dimensionValues.forEach((key, value) -> {
            System.out.println(key);
            value.forEach(System.out::println);
            System.out.println("\n");
        });
    }

    private Map<String, Set<String>> getUniqueDimensionValuesOf(String path) throws IOException, DatasetIntegrityError {
        Dataset dataset = getDataset(path);
        return dataset.getData().stream()
                .map(DataPoint::getAsMap)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet()))
                );
    }

    private void printSomeDataPointsOf(String path) throws IOException, DatasetIntegrityError {
        Dataset dataset = getDataset(path);
        dataset.getData().stream()
                .limit(5)
                .forEach(System.out::println);
    }

    private boolean isSingleRegularFile(String path) {
        return Files.isRegularFile(Paths.get(path));
    }

    private void printSingle(String path) throws IOException, DatasetIntegrityError {
        Dataset dataset = getDataset(path);
        for (DataPoint dataPoint : dataset.getData()) {
            System.out.println(dataPoint);
        }
    }

    private Dataset getDataset(String path) throws DatasetIntegrityError, IOException {
        TableAndDescriptionPair tableAndDescription = new TableAndDescriptionPair(path);
        return new TableToDatasetAdapter(
                tableAndDescription.getTable(),
                tableAndDescription.getTableDescription()
        );
    }

    public void uploadSingle(String path) throws IOException, DatasetIntegrityError {
        upload(path);
    }

    public void uploadMultiple(String inputPath) throws IOException {
        Path path = Paths.get(inputPath);
        if (Files.isRegularFile(path)) {
            uploadMultipleFromList(path);
        } else {
            uploadDirectory(path);
        }
    }

    private void uploadDirectory(Path path) throws IOException {
        Collection<Path> dataFiles = FileManager.getDataFilesInDirectory(path);
        dataFiles.stream()
                .peek(file -> LOG.info("Uploading: " + file.toString()))
                .forEach(file -> {
                    try {
                        upload(file.toString());
                    } catch (IOException | DatasetIntegrityError e) {
                        e.printStackTrace();
                    }
                });
        printTransformersReport();
    }

    private void printTransformersReport() {
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

    public void uploadMultipleFromList(Path path) {
        try (Stream<String> stream = Files.lines(path)) {
            stream.forEach(line -> {
                try {
                    upload(line);
                } catch (IOException | DatasetIntegrityError e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
