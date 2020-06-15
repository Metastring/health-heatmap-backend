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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.Table;
import org.metastringfoundation.datareader.dataset.table.TableDescription;
import org.metastringfoundation.datareader.dataset.table.csv.CSVTable;

import java.io.IOException;

import static org.metastringfoundation.healthheatmap.helpers.PathManager.guessMetadataPath;

public class TableAndDescriptionPair {
    private static final Logger LOG = LogManager.getLogger(TableAndDescriptionPair.class);

    private final Table table;
    private final TableDescription tableDescription;

    public TableAndDescriptionPair(String tablePath) throws IOException, DatasetIntegrityError {
        String metadataPath = guessMetadataPath(tablePath);
        LOG.info("Assuming metadata is at " + metadataPath);

        table = CSVTable.fromPath(tablePath);
        LOG.debug("table is " + table.getTable().toString());

        tableDescription = TableDescription.fromPath(metadataPath);
        LOG.debug("Metadata is " + tableDescription);

    }

    public Table getTable() {
        return table;
    }

    public TableDescription getTableDescription() {
        return tableDescription;
    }
}
