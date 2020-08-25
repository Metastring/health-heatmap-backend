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

import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.Table;
import org.metastringfoundation.datareader.dataset.table.TableDescription;
import org.metastringfoundation.datareader.dataset.table.TableToDatasetAdapter;
import org.metastringfoundation.healthheatmap.beans.DownloadRequest;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;
import org.metastringfoundation.healthheatmap.beans.VerificationResultField;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.HealthDatasetFromDataset;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;
import org.metastringfoundation.healthheatmap.storage.ApplicationMetadataStore;
import org.metastringfoundation.healthheatmap.storage.DatasetStore;
import org.metastringfoundation.healthheatmap.storage.FileStore;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.storage.elastic.ElasticStore;
import org.metastringfoundation.healthheatmap.storage.file.FileStoreManager;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * One (and only) implementation of the application that actually does the hard work of wiring everything together.
 * Brings everything else together to make web resources work, CLI, and anything else that needs to work.
 */
@ApplicationScoped
public class ApplicationDefault implements Application {
    private final DatasetStore datasetStore;
    private final ApplicationMetadataStore metadataStore;
    private final FileStore fileStore;

    public ApplicationDefault(@ElasticStore DatasetStore datasetStore) throws IOException {
        this(datasetStore, (ApplicationMetadataStore) datasetStore, FileStoreManager.getDefault());
    }

    @Inject
    public ApplicationDefault(
            @ElasticStore DatasetStore datasetStore,
            @ElasticStore ApplicationMetadataStore metadataStore,
            FileStore fileStore
    ) {
        this.datasetStore = datasetStore;
        this.metadataStore = metadataStore;
        this.fileStore = fileStore;
    }

    @Override
    public void save(HealthDataset dataset) throws IOException {
        datasetStore.save(dataset);
    }

    @Override
    public DataQueryResult query(DataQuery dataQuery) throws IOException {
        return datasetStore.query(dataQuery);
    }

    @Override
    public List<Map<String, Object>> getAllTermsOfFields(FilterAndSelectFields filterAndFields) throws IOException {
        return datasetStore.getAllTermsOfFields(filterAndFields);
    }

    @Override
    public void factoryReset() throws IOException {
        datasetStore.factoryReset();
    }

    @Override
    public void shutdown() throws IOException {
        datasetStore.shutdown();
    }

    @Override
    public HealthDataset asHealthDataset(TableAndDescriptionPair tableAndDescriptionPair, List<DataTransformer> transformers) throws DatasetIntegrityError {
        Dataset dataset = new TableToDatasetAdapter(
                tableAndDescriptionPair.getTable(),
                tableAndDescriptionPair.getTableDescription()
        );
        return new HealthDatasetFromDataset(dataset, transformers);
    }

    @Override
    public void logDownload(DownloadRequest downloadRequest) throws IOException {
        metadataStore.logDownload(downloadRequest);
    }

    @Override
    public boolean getHealth() throws IOException {
        return datasetStore.getHealth();
    }

    @Override
    public List<VerificationResultField> verify(Table table, List<TableDescription> tableDescriptions) throws DatasetIntegrityError {
        Dataset dataset = TableToDatasetAdapter.of(table, tableDescriptions);
        Map<String, Set<String>> fieldValues = new HashMap<>();
        for (DataPoint dataPoint : dataset.getData()) {
            dataPoint.forEach((key, value) -> fieldValues.computeIfAbsent(key, k -> new HashSet<>()).add(value));
        }
        return fieldValues.entrySet().stream()
                .map(entry -> new VerificationResultField(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void save(InputStream in, String fileNameWithRelativePath) throws IOException {
        fileStore.save(in, fileNameWithRelativePath);
    }

    @Override
    public void replaceRootDirectoryWith(Path sourceDirectoryRoot) throws IOException {
        fileStore.replaceRootDirectoryWith(sourceDirectoryRoot);
    }

    @Override
    public String getDataFilesDirectory() {
        return fileStore.getDataFilesDirectory();
    }

    @Override
    public List<String> getDataFiles(String path) throws IOException {
        return fileStore.getDataFiles(Path.of(path)).stream()
                .map(fileStore::getRelativeName)
                .collect(Collectors.toList());
    }

    @Override
    public void makeAvailableInAPI(String path) throws IOException, DatasetIntegrityError {
        TableDatasetInterpreter tableDatasetInterpreter = getTableDatasetInterpreterWithTransformersIfAvailable();

        String uploadPath = fileStore.getAbsolutePath(path);

        if (Files.isDirectory(Path.of(uploadPath))) {
            tableDatasetInterpreter.uploadMultiple(uploadPath);
        } else {
            tableDatasetInterpreter.upload(uploadPath);
        }
    }

    @Override
    public void dryMakeAvailableInAPI(String path) throws IOException, DatasetIntegrityError {
        TableDatasetInterpreter tableDatasetInterpreter = new TableDatasetInterpreter(this);
        tableDatasetInterpreter.print(path);
    }

    @Nonnull
    private TableDatasetInterpreter getTableDatasetInterpreterWithTransformersIfAvailable() throws IOException {
        String transformersDirectory = fileStore.getTransformersDirectory();
        TableDatasetInterpreter tableDatasetInterpreter;
        if (transformersDirectory != null && !transformersDirectory.isEmpty()) {
            List<DataTransformer> transformers = Stream.of(
                    List.of(new DataTransformerForEntityType()),
                    DataTransformersReader.getFromPath(Paths.get(transformersDirectory)).getTransformers(),
                    List.of(new DataTransformerForDates())
            ).flatMap(Collection::stream)
                    .collect(Collectors.toList());
            tableDatasetInterpreter = new TableDatasetInterpreter(
                    this,
                    transformers
            );
        } else {
            tableDatasetInterpreter = new TableDatasetInterpreter(this);
        }
        return tableDatasetInterpreter;
    }
}
