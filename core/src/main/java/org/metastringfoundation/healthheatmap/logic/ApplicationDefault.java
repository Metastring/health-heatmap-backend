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
import org.metastringfoundation.healthheatmap.beans.*;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;
import org.metastringfoundation.healthheatmap.logic.etl.TableDatasetInterpreter;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.storage.elastic.ElasticStore;

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
    private final DatafilesManager datafilesManager;
    private final HealthDatasetsManager healthDatasetsManager;
    private final TransformersManager transformersManager;
    private final DimensionsManager dimensionsManager;

    @Inject
    public ApplicationDefault(
            @ElasticStore DatasetStore datasetStore,
            @ElasticStore ApplicationMetadataStore metadataStore,
            FileStore fileStore,
            DatafilesManager datafilesManager,
            HealthDatasetsManager healthDatasetsManager,
            TransformersManager transformersManager,
            DimensionsManager dimensionsManager
    ) {
        this.datasetStore = datasetStore;
        this.metadataStore = metadataStore;
        this.fileStore = fileStore;
        this.datafilesManager = datafilesManager;
        this.healthDatasetsManager = healthDatasetsManager;
        this.transformersManager = transformersManager;
        this.dimensionsManager = dimensionsManager;
    }

    @Override
    public void save(HealthDataset dataset) throws IOException {
        datasetStore.save(dataset);
    }


    private void save(Map<String, HealthDataset> healthDatasets) throws IOException {
        int count = 0;
        for (Map.Entry<String, HealthDataset> entry : healthDatasets.entrySet()) {
            count += 1;
            System.out.print("\rSaving " + count + "/" + healthDatasets.entrySet().size() + ": " + entry.getKey());
            save(entry.getValue());
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
        metadataStore.factoryReset();
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
    public void markDatafileAsSaved(String datafile) throws IOException {
        metadataStore.markDatafileAsSaved(datafile);
    }

    @Override
    public boolean getHealth() throws IOException {
        return datasetStore.getHealth();
    }

    @Override
    public List<String> getSavedDataFiles() throws IOException {
        return metadataStore.getSavedDataFiles();
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
    public List<VerificationResultField> verify(HealthDataset dataset) {
        Map<String, Set<String>> fieldValues = new LinkedHashMap<>();
        for (Map<String, String> dataPoint : dataset.getData()) {
            dataPoint.forEach((key, value) -> fieldValues.computeIfAbsent(key, k -> new HashSet<>()).add(value));
        }
        return fieldValues.entrySet().stream()
                .map(entry -> new VerificationResultField(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(VerificationResultField::getName)).collect(Collectors.toList());
    }

    @Override
    public List<VerificationResultField> verify(String filename) throws DatasetIntegrityError, IOException {
        Optional<Dataset> dataset = datafilesManager.getDatasetByName(filename);
        if (dataset.isPresent()) {
            return verify(dataset.get());
        } else {
            throw new IOException("No such file " + filename);
        }
    }

    @Override
    public List<VerificationResultField> verifyAugmented(List<String> filenames) throws IOException {
        HealthDatasetBatchRead datasetBatchRead = getTheseDatasets(filenames);
        // TODO: Multiple datasets should work
        if (datasetBatchRead.getDatasets().size() > 0) {
            HealthDataset dataset = datasetBatchRead.getDatasets().get(filenames.get(0));
            dataset = dimensionsManager.augmentDatasetsWithDimensionInfo(List.of(dataset)).get(0);
            return verify(dataset);
        } else {
            throw new IOException("Couldn't read such file  " + filenames.get(0));
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
        return datafilesManager.getAllDatasets().stream()
                .map(DatasetPointer::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void refreshTransformers() throws IOException {
        LOG.info("Refreshing transformers");
        transformersManager.refresh();
    }

    public Map<String, List<String>> getFieldsAssociatedWithIndicator(String indicatorId) {
        return dimensionsManager.fieldsAssociatedWithIndicator(indicatorId);
    }

    @Override
    public Map<String, List<String>> getFieldsPossibleAt(Filter filter) throws IOException {
        List<String> knownDimensions = dimensionsManager.getKnownDimensions();
        return getDimensionsPossibleAt(knownDimensions, filter);
    }


    @Override
    public Map<String, List<String>> getDimensionsPossibleAt(List<String> knownDimensions, Filter filter) throws IOException {
        return datasetStore.getDimensionsPossibleAt(knownDimensions, filter);
    }


    @Override
    public HealthDatasetBatchRead getTheseDatasets(List<String> names) {
        List<DatasetPointer> datasetPointers = datafilesManager.getTheseDatasets(names);
        return TableDatasetInterpreter.readHealthDatasetBatch(datasetPointers);
    }


    @Override
    public List<String> getAllIndicatorsWithAssociations() {
        return dimensionsManager.getAllIndicatorsWithAssociations();
    }

    @Override
    public Map<Map<String, String>, List<Map<String, String>>> getTransformerRules(String transformerName) {
        return transformersManager.getThese(List.of(TransformerMeta.of(transformerName))).get(0).getRules();
    }

    @Override
    public List<Map<String, String>> getTransformerFailures(String transformerName) {
        return transformersManager.getThese(List.of(TransformerMeta.of(transformerName))).get(0).getUnmatchedKeysFound();
    }

    @Override
    public List<String> getListOfTransformers() {
        return transformersManager.getAllNames();
    }

    @Override
    public Map<DataPoint, Map<String, String>> getErrorsOfDatafile(String filename) {
        return healthDatasetsManager.getDatasetsUnderNameWithAugmentation(filename).get(filename).getDataPointsWithError();
    }

    @Override
    public void refreshDatasets() throws IOException, DatasetIntegrityError {
        LOG.info("Refreshing datasets");
        datafilesManager.refresh();
    }

    @Override
    public void refreshDimensions() throws IOException {
        LOG.info("Refreshing dimensions");
        dimensionsManager.refresh();
    }

    @Override
    public void reloadMemoryStores() throws IOException {
        LOG.info("Reading already saved datasets");
        List<String> datasetsUploaded = metadataStore.getSavedDataFiles();
        LOG.info("Found about " + datasetsUploaded.size() + " datasets.");
        Map<String, HealthDataset> datasetsReloaded = healthDatasetsManager.getDatasetsWithAugmentation(datasetsUploaded);
        dimensionsManager.persistAssociationWithIndicator(datasetsReloaded.values());
    }

    @Override
    public void makeAvailableInAPI(String path) throws IOException {
        Map<String, HealthDataset> datasetsAtPath = healthDatasetsManager.getDatasetsUnderNameWithAugmentation(path);
        LOG.info("Saving " + datasetsAtPath.keySet().size() + " datasets. This might take a while");
        save(datasetsAtPath);
        dimensionsManager.persistAssociationWithIndicator(datasetsAtPath.values());
        for (String file : datasetsAtPath.keySet()) {
            markDatafileAsSaved(file);
        }
        LOG.info("Here are the datasets with errors");
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
