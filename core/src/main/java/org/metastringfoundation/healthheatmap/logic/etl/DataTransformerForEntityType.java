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

package org.metastringfoundation.healthheatmap.logic.etl;

import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;

import java.util.List;
import java.util.Map;

public class DataTransformerForEntityType implements DataTransformer {
    @Override
    public List<DataPoint> transform(DataPoint data) {
        if (data.containsKey("entity.district")) {
            data.put("entity.type", "DISTRICT");
        } else if (data.containsKey("entity.state")) {
            data.put("entity.type", "STATE");
        } else {
            String entityType = null;
            for (String key : data.keySet()) {
                if (key.startsWith("entity.")) {
                    if (entityType == null) {
                        entityType = key.substring("entity.".length());
                    } else {
                        throw new IllegalArgumentException("Multiple entity.* found. Don't know how to determine entity.type");
                    }
                }
            }
        }
        return List.of(data);
    }

    @Override
    public List<Map<String, String>> getUnmatchedKeysFound() {
        return List.of();
    }

    @Override
    public Map<String, String> getKeyApplicable(DataPoint data) {
        return Map.of("entity.district", data.getOrDefault("entity.district", ""), "entity.state", data.getOrDefault("entity.state", ""));
    }

    @Override
    public Map<Map<String, String>, List<Map<String, String>>> getRules() {
        return Map.of();
    }
}
