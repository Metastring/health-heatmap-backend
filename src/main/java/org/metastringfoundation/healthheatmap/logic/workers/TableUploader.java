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

package org.metastringfoundation.healthheatmap.logic.workers;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.dataset.Dataset;
import org.metastringfoundation.healthheatmap.dataset.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.dataset.table.csv.CSVTable;
import org.metastringfoundation.healthheatmap.dataset.table.csv.CSVTableDescription;
import org.metastringfoundation.healthheatmap.dataset.table.csv.CSVTableToDatasetAdapter;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.DefaultApplication;
import org.metastringfoundation.healthheatmap.logic.errors.ApplicationError;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is a utility that helps upload data directly from command line
 */
public class TableUploader {
    private static final Logger LOG = LogManager.getLogger(TableUploader.class);

    /**
     * Uploads the data into the database of the application.
     *
     * @param path - path to the CSV file that contains data
     */
    public static void upload(String path) {
        CSVTable table = null;
        CSVTableDescription tableDescription = null;
        Dataset dataset;

        Path basedir = Paths.get(path).getParent();
        String fileName = Paths.get(path).getFileName().toString();
        String fileNameWithoutExtension = FilenameUtils.removeExtension(fileName);
        String metadataPath = Paths.get(basedir.toString(), fileNameWithoutExtension + ".metadata.json").toString();
        LOG.info("basedir is " + basedir);
        LOG.info("Assuming metadata is at " + metadataPath);
        try {
            table = CSVTable.fromPath(path);
            LOG.debug("table is " + table.getTable().toString());
        } catch (DatasetIntegrityError datasetIntegrityError) {
            datasetIntegrityError.printStackTrace();
        }

        try {
            tableDescription = CSVTableDescription.fromPath(metadataPath);
            LOG.debug("Metadata is " + tableDescription);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Application application = new DefaultApplication();
        dataset = new CSVTableToDatasetAdapter(table, tableDescription);
        try {
            Long uploadId = application.saveDataset(dataset);
            String tableName = "upload_" + uploadId.toString();
            application.saveTable(tableName, table);
            LOG.info("Done persisting dataset");
            application.shutDown();
        } catch (ApplicationError applicationError) {
            applicationError.printStackTrace();
        }
    }
}
