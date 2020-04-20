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
import org.metastringfoundation.healthheatmap.cli.CLI;
import org.metastringfoundation.healthheatmap.cli.Indicators;
import org.metastringfoundation.healthheatmap.cli.TableUploader;
import org.metastringfoundation.healthheatmap.web.Server;

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IllegalArgumentException {
        try {
            CommandLine commandLine = new CLI().parse(args);

            String path = commandLine.getOptionValue("path");
            String type = commandLine.getOptionValue("type");
            boolean batch = commandLine.hasOption("batch");
            String direction = commandLine.getOptionValue("direction");
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
                if (type == null || type.equals("data")) {
                    if (batch) {
                        TableUploader.uploadMultiple(path);
                    } else {
                        TableUploader.uploadSingle(path);
                    }
                }
                if (type != null && type.equals("indicators")) {
                    if (direction == null || direction.equals("in")) {
                        Indicators.upload(path);
                    }
                    if (direction != null && direction.equals("out")) {
                        Indicators.download(path);
                    }
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
