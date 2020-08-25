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

package org.metastringfoundation.healthheatmap.storage.file;

import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.metastringfoundation.healthheatmap.helpers.FileManager;
import org.metastringfoundation.healthheatmap.storage.FileStore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@ApplicationScoped
public class FileStoreManager implements FileStore {
    private static final String DEFAULT_DATA_DIR = "/tmp/hhm_data";
    private final Path dataDir;

    public static FileStoreManager getDefault() throws IOException {
        return new FileStoreManager(DEFAULT_DATA_DIR);
    }

    @Inject
    public FileStoreManager(@ConfigProperty(name = "hhm.datadir", defaultValue = DEFAULT_DATA_DIR) String dataDir) throws IOException {
        this.dataDir = Paths.get(dataDir);
        Files.createDirectories(this.dataDir);
        if (!Files.isWritable(this.dataDir)) {
            throw new IOException("HHM_DATADIR (" + dataDir + ") is not writeable");
        }
    }

    @Override
    public void save(InputStream in, String fileNameWithRelativePath) throws IOException {
        Path filePath = dataDir.resolve(fileNameWithRelativePath);
        Files.createDirectories(filePath.getParent());
        Files.copy(in, filePath);
    }

    @Override
    public String getRelativeName(Path path) {
        return dataDir.relativize(path).toString();
    }

    @Override
    public void replaceRootDirectoryWith(Path sourceDirectoryRoot) throws IOException {
        FileUtils.cleanDirectory(dataDir.toFile());
        try (Stream<Path> directoryTree = Files.walk(sourceDirectoryRoot)) {
            directoryTree.forEach(source -> copy(source, dataDir.resolve(sourceDirectoryRoot.relativize(source))));
        }
    }

    @Override
    public String getTransformersDirectory() {
        return dataDir.resolve("transformers").toString();
    }

    @Override
    public String getAbsolutePath(String path) {
        return Paths.get(dataDir.toString(), path).toString();
    }

    @Override
    public List<Path> getDataFiles(Path path) throws IOException {
        return FileManager.getDataFilesInDirectory(path);
    }

    private void copy(Path source, Path destination) {
        try {
            Files.copy(source, destination, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
