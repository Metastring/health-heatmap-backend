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

package org.metastringfoundation.healthheatmap.helpers;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FileManager {
    private static final Logger LOG = Logger.getLogger(FileManager.class);

    public static List<Path> getDataFilesInDirectory(Path startingDir) throws IOException {
        return Files.walk(startingDir)
                .peek(file -> LOG.debug("Evaluating: " + file.toString()))
                .filter(Files::isRegularFile)
                .peek(file -> LOG.debug("Regular file: " + file.toString()))
                .filter(file -> file.toString().endsWith(".csv"))
                .peek(file -> LOG.debug("Selected: " + file.toString()))
                .map(Path::toAbsolutePath)
                .sorted(Path::compareTo)
                .collect(Collectors.toList());
    }

    public static String readFileAsString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Path> getFilesInDirectoryThatMatch(Path path, Predicate<Path> pathCondition) throws IOException {
        return Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(pathCondition)
                .map(Path::toAbsolutePath)
                .sorted(Path::compareTo)
                .collect(Collectors.toList());
    }

    public static List<Path> getAllFilesInDirectory(Path startingDir) throws IOException {
        Predicate<Path> anyPath = p -> true;
        return getFilesInDirectoryThatMatch(startingDir, anyPath);
    }

    public static String dropExtension(String nameWithExtension) {
        return FilenameUtils.removeExtension(nameWithExtension);
    }

    public static List<Path> restrictToExistingFiles(List<Path> files) {
        return files.stream()
                .filter(Files::exists)
                .collect(Collectors.toList());
    }
}
