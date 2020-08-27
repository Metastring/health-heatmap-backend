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

package org.metastringfoundation.healthheatmap.helpers;

import org.jboss.logging.Logger;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.Table;
import org.metastringfoundation.datareader.dataset.table.TableDescription;
import org.metastringfoundation.datareader.dataset.table.csv.CSVTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.metastringfoundation.healthheatmap.helpers.PathManager.guessMetadataPath;
import static org.metastringfoundation.healthheatmap.helpers.PathManager.guessRootMetadataPath;

public class TableAndDescriptionPair {
    private static final Logger LOG = Logger.getLogger(TableAndDescriptionPair.class);

    private final Table table;
    private final TableDescription tableDescription;

    public TableAndDescriptionPair(Path tablePath) throws IOException, DatasetIntegrityError {
        table = new CSVTable(tablePath);
        LOG.debug("table is " + table.getTable().toString());

        List<Path> metadataFilesApplicable = List.of(
                guessRootMetadataPath(tablePath),
                guessMetadataPath(tablePath)
        );

        List<Path> metadataThatExists = FileManager.restrictToExistingFiles(metadataFilesApplicable);

        if (metadataThatExists.size() == 0) {
            throw new IOException("There are no metadata files applicable for" + tablePath);
        }
        LOG.debug("Reading metadata files: ");
        metadataFilesApplicable.forEach(LOG::debug);

        tableDescription = calculateTableDescription(metadataThatExists);
        if (tableDescription == null) {
            throw new IOException("Couldn't read any table metadata for " + tablePath);
        }
        LOG.debug("Metadata is " + tableDescription);
    }

    private TableDescription calculateTableDescription(List<Path> metadataFilesApplicable) {
        TableDescription tableDescription = null;
        for (Path metadata: metadataFilesApplicable) {
            try {
                tableDescription = TableDescription.add(tableDescription, TableDescription.fromPath(metadata));
            } catch (IOException e) {
                LOG.error("Error in description: " + metadata);
                e.printStackTrace();
            }
        }
        return tableDescription;
    }

    public Table getTable() {
        return table;
    }

    public TableDescription getTableDescription() {
        return tableDescription;
    }
}
