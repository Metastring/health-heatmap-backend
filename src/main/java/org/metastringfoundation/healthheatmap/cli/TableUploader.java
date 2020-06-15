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

package org.metastringfoundation.healthheatmap.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.TableToDatasetAdapter;
import org.metastringfoundation.healthheatmap.helpers.TableAndDescriptionPair;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.TableSaver;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * This is a utility that helps upload data directly from command line
 */
public class TableUploader {
    private static final Logger LOG = LogManager.getLogger(TableUploader.class);

    private final Application application;

    @Inject
    public TableUploader(Application application) {
        this.application = application;
    }

    /**
     * Uploads the data into the database of the application.
     *
     * @param path - path to the CSV file that contains data
     */
    public void upload(String path) throws IOException, DatasetIntegrityError {
        TableAndDescriptionPair tableAndDescription = new TableAndDescriptionPair(path);
        TableSaver.saveTable(
                application,
                tableAndDescription.getTable(),
                tableAndDescription.getTableDescription()
        );
        LOG.info("Done persisting dataset");
    }

    public void print(String path) throws IOException, DatasetIntegrityError {
        TableAndDescriptionPair tableAndDescription = new TableAndDescriptionPair(path);
        Dataset dataset = new TableToDatasetAdapter(
                tableAndDescription.getTable(),
                tableAndDescription.getTableDescription()
        );
        for (DataPoint dataPoint : dataset.getData()) {
            System.out.println(dataPoint);
        }
    }

    public void uploadSingle(String path) throws IOException, DatasetIntegrityError {
        upload(path);
    }

    public void uploadMultiple(String path) {
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            stream.forEach(line -> {
                try {
                    upload(line);
                } catch (IOException | DatasetIntegrityError e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
