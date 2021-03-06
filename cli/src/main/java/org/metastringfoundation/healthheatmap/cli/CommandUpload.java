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

import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.logic.Application;
import picocli.CommandLine;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Dependent
@CommandLine.Command(name = "upload", description = "uploads data", mixinStandardHelpOptions = true)
public class CommandUpload implements Callable<Integer> {

    @CommandLine.Option(names = {"-p", "--path"}, description = "Path to the root of the data directory", required = true)
    String path;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Name (relative path) of the file/folder to upload", required = true)
    String name;

    @CommandLine.Option(names = {"-z", "--recreate"}, description = "Whether elastic index should be deleted before entering data")
    boolean recreateIndex;

    @CommandLine.Option(names = {"-d", "--dry"}, description = "When used, prints the dataset instead of uploading.")
    boolean dry;

    private final Application application;

    @Inject
    public CommandUpload(Application application) {
        this.application = application;
    }

    @Override
    public Integer call() throws IOException, DatasetIntegrityError {
        if (path == null || path.isEmpty() || name == null) {
            System.out.println("There can be no upload without passing in -p and -n");
            return 1;
        }
        if (recreateIndex) {
            application.factoryReset();
        }

        if (dry) {
            application.dryMakeAvailableInAPI(Paths.get(path, name).toString());
        } else {
            application.replaceRootDirectoryWith(Path.of(path));
            application.makeAvailableInAPI(name);
        }
        return 0;
    }
}
