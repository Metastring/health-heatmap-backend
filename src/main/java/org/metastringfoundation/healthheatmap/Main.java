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

package org.metastringfoundation.healthheatmap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.cli.CLI;
import org.metastringfoundation.healthheatmap.cli.DataTransformersReader;
import org.metastringfoundation.healthheatmap.cli.TableUploader;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.ApplicationDefault;
import org.metastringfoundation.healthheatmap.web.Server;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IllegalArgumentException, IOException, DatasetIntegrityError {
        try {
            CommandLine commandLine = new CLI().parse(args);

            String path = commandLine.getOptionValue("path");
            String transformersDir = commandLine.getOptionValue("transformers");
            boolean batch = commandLine.hasOption("batch");
            boolean dry = commandLine.hasOption("dry");
            boolean serverShouldStart = commandLine.hasOption("server");

            if (serverShouldStart) {
                Server.startProductionServer();
            } else if (path != null && !path.isEmpty()) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        LOG.info("Shutting down");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
                Application application = ApplicationDefault.createPreconfiguredApplicationDefault();
                TableUploader tableUploader;
                if (transformersDir != null && !transformersDir.isEmpty()) {
                    tableUploader = new TableUploader(
                            application,
                            DataTransformersReader.getFromPath(Paths.get(transformersDir)).getTransformers()
                    );
                } else {
                    tableUploader = new TableUploader(application);
                }
                if (dry) {
                    if (batch) {
                        System.out.println("Printing in batch not yet supported");
                    } else {
                        tableUploader.print(path);
                    }
                } else if (batch) {
                    tableUploader.uploadMultiple(path);
                } else {
                    tableUploader.uploadSingle(path);
                }
                application.shutdown();
            } else {
                CLI.printHelp();
            }

        } catch (ParseException e) {
            CLI.printHelp();
            System.exit(1);
        }
    }

}
