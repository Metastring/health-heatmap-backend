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

import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.storage.FileStore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DatasetsManager {
    private final FileStore fileStore;
    private final TransformersManager transformersManager;
    private List<DatasetPointer> datasetPointerList;

    @Inject
    public DatasetsManager(FileStore fileStore, TransformersManager transformersManager) throws IOException, DatasetIntegrityError {
        this.fileStore = fileStore;
        this.transformersManager = transformersManager;
        this.datasetPointerList = calculateDatasetPointerList();
    }

    public List<DatasetPointer> getAllDatasets() {
        return datasetPointerList;
    }

    public void refreshDatasets() throws IOException, DatasetIntegrityError {
        datasetPointerList = calculateDatasetPointerList();
    }

    private List<DatasetPointer> calculateDatasetPointerList() throws IOException, DatasetIntegrityError {
        List<DatasetPointer> result = new ArrayList<>();
        List<Path> csvPaths = fileStore.getFilesThatMatch(
                fileStore.getDataFilesDirectory(),
                p -> p.toString().endsWith(".csv")
        );
        for (Path csv: csvPaths) {
            result.add(getCSVDatasetPointer(csv));
        }
        return result;
    }

    private DatasetPointer getCSVDatasetPointer(Path csvPath) throws IOException, DatasetIntegrityError {
        return new CSVDatasetPointer(csvPath, fileStore, transformersManager);
    }
}
