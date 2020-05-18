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
import org.metastringfoundation.healthheatmap.cli.TableUploader;
import org.metastringfoundation.healthheatmap.logic.DefaultApplication;
import org.metastringfoundation.healthheatmap.logic.errors.ApplicationError;
import org.metastringfoundation.healthheatmap.web.Server;

import java.io.IOException;

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IllegalArgumentException, ApplicationError, IOException, DatasetIntegrityError {
        try {
            CommandLine commandLine = new CLI().parse(args);

            String path = commandLine.getOptionValue("path");
            boolean batch = commandLine.hasOption("batch");
            boolean serverShouldStart = commandLine.hasOption("server");

            if (serverShouldStart) {
                Server.startProductionServer();
            } else if (!path.isEmpty()) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        LOG.info("Shutting down");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
                TableUploader tableUploader = new TableUploader(DefaultApplication.getDefaultDefaultApplication());
                if (batch) {
                    tableUploader.uploadMultiple(path);
                } else {
                    tableUploader.uploadSingle(path);
                }
            } else {
                CLI.printHelp();
            }

        } catch (ParseException e) {
            CLI.printHelp();
            System.exit(1);
        }
    }

}
