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

import org.metastringfoundation.healthheatmap.beans.TransformerMeta;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;
import org.metastringfoundation.healthheatmap.logic.FileStore;
import org.metastringfoundation.healthheatmap.logic.TransformersManager;
import org.metastringfoundation.healthheatmap.logic.etl.DataTransformerForDates;
import org.metastringfoundation.healthheatmap.logic.etl.DataTransformerForEntityType;
import org.metastringfoundation.healthheatmap.logic.etl.DataTransformerFromSpreadsheet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;

@ApplicationScoped
public class TransformersManagerInMemory implements TransformersManager {
    private final FileStore fileStore;
    private Map<String, DataTransformer> transformers;

    @Inject
    public TransformersManagerInMemory(FileStore fileStore) throws IOException {
        this.fileStore = fileStore;
        this.transformers = readTransformers();
    }

    private Map<String, DataTransformer> readTransformers() throws IOException {
        Map<String, DataTransformer> result = new HashMap<>();
        result.put("sys/entity", new DataTransformerForEntityType());
        List<Path> files = fileStore.getFiles(fileStore.getTransformersDirectory());
        for (Path file : files) {
            String name = fileStore.getRelativeName(file);
            DataTransformer transformer = DataTransformerFromSpreadsheet.getDataTransformerCrashingOnError(
                    fileStore.getFileAsString(file)
            );
            result.put(name, transformer);
        }
        result.put("zsys/date", new DataTransformerForDates());
        return result;
    }

    @Override
    public void refresh() throws IOException {
        transformers = readTransformers();
    }

    @Override
    public List<DataTransformer> getThese(List<TransformerMeta> transformerMetaList) {
        return transformerMetaList.stream()
                .map(TransformerMeta::getName)
                .map(transformers::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<DataTransformer> getAll() {
        return transformers.entrySet().stream()
                .sorted(comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
