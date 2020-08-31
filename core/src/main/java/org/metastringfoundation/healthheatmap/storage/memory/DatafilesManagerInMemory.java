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
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.logic.DatafilesManager;
import org.metastringfoundation.healthheatmap.logic.DatasetPointer;
import org.metastringfoundation.healthheatmap.logic.FileStore;
import org.metastringfoundation.healthheatmap.logic.TransformersManager;
import org.metastringfoundation.healthheatmap.logic.etl.CSVDatasetPointer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class DatafilesManagerInMemory implements DatafilesManager {
    private static final Logger LOG = Logger.getLogger(DatafilesManagerInMemory.class);
    private final FileStore fileStore;
    private final TransformersManager transformersManager;
    private List<DatasetPointer> datasetPointerList;

    @Inject
    public DatafilesManagerInMemory(FileStore fileStore, TransformersManager transformersManager) throws IOException, DatasetIntegrityError {
        this.fileStore = fileStore;
        this.transformersManager = transformersManager;
        this.datasetPointerList = calculateDatasetPointerList();
        LOG.info(datasetPointerList.size() + " datasets loaded to manager");
    }

    @Override
    public List<DatasetPointer> getAllDatasets() {
        return datasetPointerList;
    }

    @Override
    public List<DatasetPointer> getTheseDatasets(List<String> filenames) {
        return datasetPointerList.stream()
                .filter(datasetPointer -> filenames.contains(datasetPointer.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void refresh() throws IOException, DatasetIntegrityError {
        datasetPointerList = calculateDatasetPointerList();
    }

    @Override
    public Optional<Dataset> getDatasetByName(String filename) throws IOException, DatasetIntegrityError {
        Optional<DatasetPointer> datasetPointer = datasetPointerList.stream()
                .filter(pointer -> pointer.getName().equals(filename))
                .findAny();
        if (datasetPointer.isPresent()) {
            return Optional.of(datasetPointer.get().getDataset());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<String> getDatasetsAtName(String name) {
        if (name == null || name.equals("")) {
            return datasetPointerList.stream()
                    .map(DatasetPointer::getName)
                    .collect(Collectors.toList());
        } else {
            return datasetPointerList.stream()
                    .map(DatasetPointer::getName)
                    .filter(ds -> ds.equals(name) || ds.startsWith(name + "/") || (name.endsWith("/") && ds.startsWith(name))) // NOPMD - improved readability of operator precedence
                    .collect(Collectors.toList());
        }
    }

    private List<DatasetPointer> calculateDatasetPointerList() throws IOException, DatasetIntegrityError {
        List<DatasetPointer> result = new ArrayList<>();
        List<Path> csvPaths = fileStore.getFilesThatMatch(
                fileStore.getDataFilesDirectory(),
                p -> p.toString().endsWith(".csv")
        );
        for (Path csv : csvPaths) {
            LOG.debug("Reading " + csv);
            result.add(getCSVDatasetPointer(csv));
        }
        return result;
    }

    private DatasetPointer getCSVDatasetPointer(Path csvPath) throws IOException, DatasetIntegrityError {
        return new CSVDatasetPointer(csvPath, fileStore, transformersManager);
    }
}
