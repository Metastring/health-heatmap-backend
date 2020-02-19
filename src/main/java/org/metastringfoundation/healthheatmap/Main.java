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
import org.metastringfoundation.healthheatmap.dataset.*;
import org.metastringfoundation.healthheatmap.housekeeping.DatabaseProfiler;
import org.metastringfoundation.healthheatmap.storage.DataStorer;
import org.metastringfoundation.healthheatmap.storage.MultiplexDataStorer;
import org.metastringfoundation.healthheatmap.web.Server;

public class Main {


    public static void main(String[] args) throws IllegalArgumentException {
        try {
            CommandLine commandLine = new CLI().parse(args);

            DataStorer dataStorer = new MultiplexDataStorer();

            String path = commandLine.getOptionValue("path");
            String datasetName = commandLine.getOptionValue("name");
            String rangeReference = commandLine.getOptionValue("ranges");
            boolean random = commandLine.hasOption("random");
            boolean profiler = commandLine.hasOption("profiler");
            boolean serverShouldStart = commandLine.hasOption("server");
            String profilerAction = commandLine.getOptionValue("profiler");

            if (random) {
                Dataset dataset;
                int GENERATE_THIS_MANY_DATASETS = 20;
                for (int datasetNumber = 0; datasetNumber < GENERATE_THIS_MANY_DATASETS; datasetNumber++) {
                    dataset = new RandomDataset.builder()
                            .entities(500)
                            .indicators(500)
                            .name("Random Dataset " + datasetNumber)
                            .build();

                    System.out.println("adding dataset " + datasetNumber);
                    dataStorer.addDataset(dataset);
                }
            } else if (profiler) {
                System.out.println("Starting profiler...");
                new DatabaseProfiler(profilerAction).run();
                System.out.println("Ended profiling.");
            } else if (serverShouldStart) {
                Server.startServer();
            } else {
                Dataset dataset = new CSVDataset
                        .builder()
                        .metadata(new DatasetMetadata(datasetName))
                        .path(path)
                        .ranges(rangeReference)
                        .build();

                dataStorer.addDataset(dataset);
            }

        } catch (ParseException e) {
            CLI.printHelp();
            System.exit(1);
        } catch (DatasetIntegrityError e) {
            System.out.println("The data has some problem");
            e.printStackTrace();
        }
    }

}
