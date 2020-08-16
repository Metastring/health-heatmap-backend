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

import org.apache.commons.cli.Option;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;
import org.metastringfoundation.healthheatmap.logic.DataTransformerForDates;
import org.metastringfoundation.healthheatmap.logic.DataTransformerForEntityType;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "upload", description = "uploads data")
public class CommandUpload implements Callable<Integer> {

    @CommandLine.Option(names = {"-p", "--path"}, description = "Path to the file/directory to be uploaded", required = true)
    String path;

    @CommandLine.Option(names = {"-b", "--batch"}, description = "Whether the path specified requires a bulk upload (i.e., is a directory) (only use with -p)")
    Boolean batch;

    @CommandLine.Option(names = {"-z", "--recreate"}, description = "Whether elastic index should be deleted before entering data")
    Boolean recreateIndex;

    @CommandLine.Option(names = {"-d", "--dry"}, description = "When used, prints the dataset instead of uploading. Only supported when -b is not given.")
    Boolean dry;

    @CommandLine.Option(names = {"-t", "--transformers"}, description = "Directory which contains the data transformers that need to run on the data before it gets uploaded")
    String transformersDirectory;


    @Inject
    Application application;

    @Override
    public Integer call() throws IOException, DatasetIntegrityError {
        if (path == null || path.isEmpty()) {
            System.out.println("There can be no upload without passing in -p");
            return 1;
        }
        if (recreateIndex) {
            application.factoryReset();
        }
        TableUploader tableUploader;
        if (transformersDirectory != null && !transformersDirectory.isEmpty()) {
            List<DataTransformer> transformers = Stream.of(
                    List.of(new DataTransformerForEntityType()),
                    DataTransformersReader.getFromPath(Paths.get(transformersDirectory)).getTransformers(),
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
        return 0;
    }
}
