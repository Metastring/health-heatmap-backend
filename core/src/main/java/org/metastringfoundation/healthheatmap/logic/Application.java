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
import org.metastringfoundation.datareader.dataset.table.Table;
import org.metastringfoundation.datareader.dataset.table.TableDescription;
import org.metastringfoundation.healthheatmap.beans.VerificationResultField;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface Application extends DatasetStore, ApplicationMetadataStore {
    void shutdown() throws IOException;

    HealthDataset asHealthDataset(TableAndDescriptionPair tableAndDescriptionPair, List<DataTransformer> transformers) throws DatasetIntegrityError;

    boolean getHealth() throws IOException;

    List<VerificationResultField> verify(Table table, List<TableDescription> tableDescriptions) throws DatasetIntegrityError;
    List<VerificationResultField> verify(String filename) throws DatasetIntegrityError, IOException;

    void refreshDatasets() throws IOException, DatasetIntegrityError;

    void refreshDimensions() throws IOException;

    void makeAvailableInAPI(String path) throws IOException;

    void save(List<HealthDataset> healthDatasets) throws IOException;

    void dryMakeAvailableInAPI(String path) throws IOException, DatasetIntegrityError;
    void dryMakeAvailableInAPIConcise(String path) throws IOException, DatasetIntegrityError;

    void save(InputStream in, String fileNameWithRelativePath) throws IOException;
    void replaceRootDirectoryWith(Path sourceDirectoryRoot) throws IOException, DatasetIntegrityError;

    String getDataFilesDirectory();

    List<String> getDataFiles() throws IOException;

    void refreshTransformers() throws IOException;
}
