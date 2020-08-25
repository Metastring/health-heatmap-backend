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
import java.util.concurrent.Callable;

@Dependent
@CommandLine.Command(name = "print", description = "prints a dataset", mixinStandardHelpOptions = true)
public class CommandPrint implements Callable<Integer> {

    @CommandLine.Option(names = {"-p", "--path"}, description = "Path to the dataset", required = true)
    String path;

    private final Application application;

    @Inject
    public CommandPrint(Application application) {
        this.application = application;
    }

    @Override
    public Integer call() throws IOException, DatasetIntegrityError {
        if (path == null || path.isEmpty()) {
            System.out.println("There can be no print without passing in -p");
            return 1;
        }
        if (application == null) {
            throw new RuntimeException("Injection of application failed");
        }
        application.dryMakeAvailableInAPI(path);
        return 0;
    }
}
