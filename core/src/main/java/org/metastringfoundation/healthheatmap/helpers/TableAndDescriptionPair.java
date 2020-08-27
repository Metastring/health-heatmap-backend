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
import org.metastringfoundation.datareader.dataset.table.Table;
import org.metastringfoundation.datareader.dataset.table.TableDescription;
import org.metastringfoundation.healthheatmap.beans.HealthDatasetMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class TableAndDescriptionPair {
    private static final Logger LOG = Logger.getLogger(TableAndDescriptionPair.class);
    private final Table table;
    private final TableDescription tableDescription;

    public TableAndDescriptionPair(Table table, List<Path> metadataFilesApplicable) throws IOException {
        this.table = table;
        LOG.debug("table is " + table.getTable().toString());
        List<Path> metadataThatExists = FileManager.restrictToExistingFiles(metadataFilesApplicable);

        if (metadataThatExists.size() == 0) {
            throw new IOException("There are no metadata files");
        }
        LOG.debug("Reading metadata files: ");
        metadataFilesApplicable.forEach(LOG::debug);

        tableDescription = calculateTableDescription(metadataThatExists);
        if (tableDescription == null) {
            throw new IOException("Couldn't read any table metadata");
        }
        LOG.debug("Metadata is " + tableDescription);
    }

    private TableDescription calculateTableDescription(List<Path> metadataFilesApplicable) {
        TableDescription tableDescription = null;
        for (Path metadata: metadataFilesApplicable) {
            try {
                tableDescription = TableDescription.add(tableDescription, TableDescription.fromPath(metadata), TableAndDescriptionPair::metadataAdder);
            } catch (IOException e) {
                LOG.error("Error in description: " + metadata);
                e.printStackTrace();
            }
        }
        return tableDescription;
    }

    private static Object metadataAdder(Object first, Object second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        HealthDatasetMetadata firstMeta = Jsonizer.convert(first, HealthDatasetMetadata.class);
        HealthDatasetMetadata secondMeta = Jsonizer.convert(second, HealthDatasetMetadata.class);
        HealthDatasetMetadata sum = new HealthDatasetMetadata();
        if (secondMeta.getTransformers() != null) {
            sum.setTransformers(secondMeta.getTransformers());
        } else {
            sum.setTransformers(firstMeta.getTransformers());
        }
        return sum;
    }

    public Table getTable() {
        return table;
    }

    public TableDescription getTableDescription() {
        return tableDescription;
    }
}
