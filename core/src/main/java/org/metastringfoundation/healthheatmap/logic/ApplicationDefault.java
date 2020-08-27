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
import org.metastringfoundation.datareader.dataset.table.Table;
import org.metastringfoundation.datareader.dataset.table.TableDescription;
import org.metastringfoundation.datareader.dataset.table.TableToDatasetAdapter;
import org.metastringfoundation.healthheatmap.beans.DownloadRequest;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;
import org.metastringfoundation.healthheatmap.beans.HealthDatasetBatchRead;
import org.metastringfoundation.healthheatmap.beans.VerificationResultField;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;
import org.metastringfoundation.healthheatmap.logic.etl.TableDatasetInterpreter;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.storage.elastic.ElasticStore;
import org.metastringfoundation.healthheatmap.storage.memory.DimensionsManagerInMemory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * One (and only) implementation of the application that actually does the hard work of wiring everything together.
 * Brings everything else together to make web resources work, CLI, and anything else that needs to work.
 */
@ApplicationScoped
public class ApplicationDefault implements Application {
    private static final Logger LOG = Logger.getLogger(ApplicationDefault.class);

    private final DatasetStore datasetStore;
    private final ApplicationMetadataStore metadataStore;
    private final FileStore fileStore;
    private final DatasetsManager datasetsManager;
    private final TransformersManager transformersManager;
    private final DimensionsManagerInMemory dimensionsManager;

    @Inject
    public ApplicationDefault(
            @ElasticStore DatasetStore datasetStore,
            @ElasticStore ApplicationMetadataStore metadataStore,
            FileStore fileStore,
            DatasetsManager datasetsManager,
            TransformersManager transformersManager,
            DimensionsManagerInMemory dimensionsManager
    ) {
        this.datasetStore = datasetStore;
        this.metadataStore = metadataStore;
        this.fileStore = fileStore;
        this.datasetsManager = datasetsManager;
        this.transformersManager = transformersManager;
        this.dimensionsManager = dimensionsManager;
    }

    @Override
    public void save(HealthDataset dataset) throws IOException {
        datasetStore.save(dataset);
    }

    @Override
    public void save(List<HealthDataset> healthDatasets) throws IOException {
        for (HealthDataset healthDataset : healthDatasets) {
            save(healthDataset);
        }
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
        return TableDatasetInterpreter.asHealthDataset(tableAndDescriptionPair, transformers);
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
        return verify(dataset);
    }

    @Override
    public List<VerificationResultField> verify(Dataset dataset) {
        Map<String, Set<String>> fieldValues = new HashMap<>();
        for (DataPoint dataPoint : dataset.getData()) {
            dataPoint.forEach((key, value) -> fieldValues.computeIfAbsent(key, k -> new HashSet<>()).add(value));
        }
        return fieldValues.entrySet().stream()
                .map(entry -> new VerificationResultField(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<VerificationResultField> verify(String filename) throws DatasetIntegrityError, IOException {
        Optional<Dataset> dataset = datasetsManager.getDatasetByName(filename);
        if (dataset.isPresent()) {
            return verify(dataset.get());
        } else {
            throw new IOException("No such file " + filename);
        }
    }

    @Override
    public void save(InputStream in, String fileNameWithRelativePath) throws IOException {
        fileStore.save(in, fileNameWithRelativePath);
    }

    @Override
    public void replaceRootDirectoryWith(Path sourceDirectoryRoot) throws IOException, DatasetIntegrityError {
        fileStore.replaceRootDirectoryWith(sourceDirectoryRoot);
        refreshDatasets();
        refreshTransformers();
        refreshDimensions();
    }

    @Override
    public String getDataFilesDirectory() {
        return fileStore.getDataFilesDirectory().toString();
    }

    @Override
    public List<String> getDataFiles() {
        return datasetsManager.getAllDatasets().stream()
                .map(DatasetPointer::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void refreshTransformers() throws IOException {
        LOG.info("Refreshing transformers");
        transformersManager.refresh();
    }

    @Override
    public void refreshDatasets() throws IOException, DatasetIntegrityError {
        LOG.info("Refreshing datasets");
        datasetsManager.refresh();
    }

    @Override
    public void refreshDimensions() throws IOException {
        LOG.info("Refreshing dimensions");
        dimensionsManager.refresh();
    }

    @Override
    public void makeAvailableInAPI(String path) throws IOException {
        LOG.info("Uploading " + path + " to the dataset");
        HealthDatasetBatchRead healthDatasetsRead = TableDatasetInterpreter.readHealthDatasetBatch(datasetsManager.getAllDatasets());
        LOG.info("Saving " + healthDatasetsRead.getDatasets().size() + " datasets. This might take a while");
        save(healthDatasetsRead.getDatasets());
        LOG.info("Here are the datasets with errors");
        healthDatasetsRead.getErrors().forEach((filePath, error) -> LOG.error(filePath + " suffered an error: " + error.getMessage()));
        TableDatasetInterpreter.printTransformersReport(transformersManager.getAll());
    }

    @Override
    public void dryMakeAvailableInAPI(String path) throws IOException, DatasetIntegrityError {
        TableDatasetInterpreter.print(fileStore.getDataFiles(path));
    }

    @Override
    public void dryMakeAvailableInAPIConcise(String path) throws IOException, DatasetIntegrityError {
        TableDatasetInterpreter.printConcise(fileStore.getDataFiles(path));
    }
}
