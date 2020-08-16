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

import io.quarkus.runtime.QuarkusApplication;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.cli.CLI;
import org.metastringfoundation.healthheatmap.cli.DataTransformersReader;
import org.metastringfoundation.healthheatmap.cli.TableUploader;
import org.metastringfoundation.healthheatmap.logic.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainWithBells implements QuarkusApplication {
    @Override
    public int run(String... args) throws IllegalArgumentException, IOException, DatasetIntegrityError {
        try {
            CommandLine commandLine = new CLI().parse(args);

            String path = commandLine.getOptionValue("path");
            String transformersDir = commandLine.getOptionValue("transformers");
            boolean batch = commandLine.hasOption("batch");
            boolean dry = commandLine.hasOption("dry");
            boolean serverShouldStart = commandLine.hasOption("server");
            boolean recreateIndex = commandLine.hasOption("recreate");

            if (serverShouldStart) {
                System.out.println("Please use `mvn liberty:dev` to start development server");

            } else if (path != null && !path.isEmpty()) {
                Application application = ApplicationDefault.createPreconfiguredApplicationDefault();
                if (recreateIndex) {
                    application.factoryReset();
                }
                TableUploader tableUploader;
                if (transformersDir != null && !transformersDir.isEmpty()) {
                    List<DataTransformer> transformers = Stream.of(
                            List.of(new DataTransformerForEntityType()),
                            DataTransformersReader.getFromPath(Paths.get(transformersDir)).getTransformers(),
                            List.of(new DataTransformerForDates())
                    ).flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    tableUploader = new TableUploader(
                            application,
                            transformers
                    );
                } else {
                    tableUploader = new TableUploader(application);
                }
                if (dry) {
                    tableUploader.print(path);
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
        return 0;
    }
}
