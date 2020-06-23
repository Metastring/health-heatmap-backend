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

import org.metastringfoundation.healthheatmap.helpers.FileManager;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;
import org.metastringfoundation.healthheatmap.logic.DataTransformerFromSpreadsheet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DataTransformersReader {
    private final List<DataTransformer> transformers;

    public DataTransformersReader(List<Path> files) {
        this.transformers = readTransformers(files);
    }

    public static DataTransformersReader getFromPath(Path path) throws IOException {
        List<Path> files = FileManager.getFilesInDirectoryInOrder(path);
        return new DataTransformersReader(files);
    }

    private List<DataTransformer> readTransformers(List<Path> files) {
        return files.stream()
                .map(FileManager::readFileAsString)
                .map(DataTransformerFromSpreadsheet::getDataTransformerCrashingOnError)
                .collect(Collectors.toList());
    }

    public List<DataTransformer> getTransformers() {
        return transformers;
    }
}
