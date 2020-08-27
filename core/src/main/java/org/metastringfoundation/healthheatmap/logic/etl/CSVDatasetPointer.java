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

import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.Table;
import org.metastringfoundation.datareader.dataset.table.TableToDatasetAdapter;
import org.metastringfoundation.datareader.dataset.table.csv.CSVTable;
import org.metastringfoundation.healthheatmap.beans.HealthDatasetMetadata;
import org.metastringfoundation.healthheatmap.beans.TransformerMeta;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;
import org.metastringfoundation.healthheatmap.logic.DatasetPointer;
import org.metastringfoundation.healthheatmap.logic.FileStore;
import org.metastringfoundation.healthheatmap.logic.TransformersManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.metastringfoundation.healthheatmap.helpers.PathManager.guessMetadataPath;
import static org.metastringfoundation.healthheatmap.helpers.PathManager.guessRootMetadataPath;

public class CSVDatasetPointer implements DatasetPointer {
    private final Path path;
    private final FileStore fileStore;
    private final TransformersManager transformersManager;
    private final TableAndDescriptionPair tableAndDescriptionPair;

    public CSVDatasetPointer(Path path, FileStore fileStore, TransformersManager transformersManager) throws IOException, DatasetIntegrityError {
        this.path = path;
        this.fileStore = fileStore;
        this.transformersManager = transformersManager;
        this.tableAndDescriptionPair = calculateTableAndDescription();
    }

    private TableAndDescriptionPair calculateTableAndDescription() throws IOException, DatasetIntegrityError {
        Table table = new CSVTable(this.path);
        List<Path>  metadataFilesApplicable = List.of(
                        guessRootMetadataPath(this.path),
                        guessMetadataPath(this.path)
                );
        return new TableAndDescriptionPair(table, metadataFilesApplicable);
    }

    @Override
    public Dataset getDataset() throws DatasetIntegrityError {
        return new TableToDatasetAdapter(
                tableAndDescriptionPair.getTable(),
                tableAndDescriptionPair.getTableDescription()
        );
    }

    @Override
    public List<DataTransformer> getTransformers() {
        HealthDatasetMetadata metadata = Jsonizer.convert(tableAndDescriptionPair.getTableDescription().getMetadata(), HealthDatasetMetadata.class);
        if (metadata == null || metadata.getTransformers() == null) {
            return transformersManager.getAll();
        }
        List<TransformerMeta> transformerMetaList = metadata.getTransformers();
        return transformersManager.getThese(transformerMetaList);
    }

    @Override
    public String getName() {
        return fileStore.getRelativeName(path);
    }
}
