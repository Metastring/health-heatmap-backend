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

package org.metastringfoundation.healthheatmap.logic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public interface FileStore {
    void save(InputStream in, String fileNameWithRelativePath) throws IOException;
    String getRelativeName(Path filePath);
    String getRelativeName(Path file, Path directory);

    void replaceRootDirectoryWith(Path sourceDirectoryRoot) throws IOException;
    Path getTransformersDirectory();
    Path getDataFilesDirectory();
    Path getDimensionsDirectory();

    Path getAbsolutePath(Path path);
    List<Path> getDataFiles(Path path) throws IOException;
    List<Path> getDataFiles(String path) throws IOException;

    List<Path> getFilesThatMatch(Path path, Predicate<Path> pathCondition) throws IOException;

    String getFileAsString(Path file);
}
