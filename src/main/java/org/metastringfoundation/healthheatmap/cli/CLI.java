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

import org.apache.commons.cli.*;

/**
 * Wrapper CLI to parse arguments using standard libraries (Apache Commons CLI)
 */
public class CLI {
    /**
     * Option to start the server
     */
    final static Option server = new Option("s", "server", false, "Run the API server");

    /**
     * Path to a file that needs to be uploaded to the dataset
     */
    final static Option path = Option.builder("p")
            .hasArg()
            .longOpt("path")
            .desc("Path to the file/directory to be uploaded")
            .build();

    final static Option batch = Option.builder("b")
            .longOpt("batch")
            .desc("Whether the path specified requires a bulk upload (i.e., is a directory) (only use with -p)")
            .build();

    final static Option dry = Option.builder("d")
            .longOpt("dry")
            .desc("When used, prints the dataset instead of uploading. Only supported when -b is not given.")
            .build();

    final static Option transformersDirectory = Option.builder("t")
            .longOpt("transformers")
            .hasArg()
            .desc("Directory which contains the data transformers that need to run on the data before it gets uploaded")
            .build();


    public final static Options options = new Options()
            .addOption(path)
            .addOption(batch)
            .addOption(dry)
            .addOption(transformersDirectory)
            .addOption(server);

    public CommandLine parse(String[] args) throws IllegalArgumentException, ParseException {

        final CommandLineParser commandLineParser = new DefaultParser();
        try {
            return commandLineParser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("data-reader", options);
    }
}
