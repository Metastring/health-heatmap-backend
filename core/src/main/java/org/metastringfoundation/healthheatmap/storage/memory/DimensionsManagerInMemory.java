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

package org.metastringfoundation.healthheatmap.storage.memory;

import org.metastringfoundation.healthheatmap.helpers.FileManager;
import org.metastringfoundation.healthheatmap.helpers.ReadCSVAsMap;
import org.metastringfoundation.healthheatmap.logic.FileStore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DimensionsManagerInMemory {
    private final FileStore fileStore;
    //         dimension    id          param   value
    private Map<String, Map<String, Map<String, String>>> backingMap;
    // eg:     entity       KL_KN       district kannur

    @Inject
    public DimensionsManagerInMemory(FileStore fileStore) throws IOException {
        this.fileStore = fileStore;
        this.backingMap = calculateDimensions();
    }

    private Map<String, Map<String, Map<String, String>>> calculateDimensions() throws IOException {
        Map<String, Map<String, Map<String, String>>> allDimensions = new HashMap<>();
        List<Path> files = fileStore.getFiles(fileStore.getDimensionsDirectory());
        for (Path file: files) {
            String nameWithExtension = fileStore.getRelativeName(file, fileStore.getDimensionsDirectory());
            String name = FileManager.dropExtension(nameWithExtension);
            Map<String, Map<String, String>> currentDimension = new HashMap<>();
            allDimensions.put(name, currentDimension);
            ReadCSVAsMap.get(file).forEach(rec -> {
                String id = rec.get("id");
                currentDimension.put(id, rec);
            });
        }
        return allDimensions;
    }

    public void refresh() throws IOException {
        this.backingMap = calculateDimensions();
    }

    public Set<String> getValidIdsOf(String dimension) {
        return backingMap.get(dimension).keySet();
    }
    public Boolean idExists(String dimension, String id) {
        return idExists(backingMap.get(dimension), id);
    }
    private Boolean idExists(Map<String, Map<String, String>> dimensionMap, String id) {
        return dimensionMap.containsKey(id);
    }

    public Boolean dimensionExists(String dimension) {
        return backingMap.containsKey(dimension);
    }

    public void createDimension(String dimension) {
        if (dimensionExists(dimension)) {
            throw new IllegalArgumentException("Dimension " + dimension + " already exists");
        } else {
            backingMap.put(dimension, new HashMap<>());
        }
    }

    public void addValuesToDimension(String dimension, Map<String, Map<String, String>> values) {
        Map<String, Map<String, String>> thisDimension = backingMap.get(dimension);
        values.keySet().forEach(id -> {
            if (idExists(thisDimension, id)) {
                throw new IllegalArgumentException("ID " + id + " already exists in dimension " + dimension);
            }
        });
        thisDimension.putAll(values);
    }

}
