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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListAndMapUtils {

    public static <T> Map<String, String> getStringOnlyMap(Map<String, T> input) {
        return input.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), sanitized(entry.getValue())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private static <T> String sanitized(T value) {
        if (value == null) {
            return "";
        } else {
            return (String) value;
        }
    }

    public static <T> List<Map<String, String>> getListOfStringOnlyMaps(List<Map<String, T>> inputMaps) {
        return inputMaps.stream()
                .map(ListAndMapUtils::getStringOnlyMap)
                .collect(Collectors.toList());
    }

    public static List<Map<String, String>> filterKeys(List<Map<String, String>> result, List<String> required) {
        return result.stream()
                .map(unfiltered -> filterKeys(unfiltered, required))
                .collect(Collectors.toList());
    }

    public static Map<String, String> filterKeys(Map<String, String> unfiltered, List<String> required) {
        if (required == null || required.size() == 0) {
            return unfiltered;
        }
        return unfiltered.entrySet().stream()
                .filter(entry -> required.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static List<Map<String, String>> reshapeCast(List<Map<String, String>> result, String dimensionToTranspose) {
        Map<Map<String, String>, Map<String, String>> pivotalMap = new HashMap<>();
        for (Map<String, String> record : result) {
            String value = record.remove("value");
            String dimension = record.remove(dimensionToTranspose);
            pivotalMap.computeIfAbsent(record, whatever -> new StrictHashMap<>()).put(dimension, value);
        }
        // merge key and value maps into key
        pivotalMap.forEach(Map::putAll);
        return new ArrayList<>(pivotalMap.keySet());
    }

    public static List<Map<String, String>> reshapeCast(List<Map<String, String>> result, String dimensionToTranspose, List<String> thirdDimension) {
        // 3rd dimension params                         transposed values
        Map<Map<String, String>, Map<Map<String, String>, Map<String, String>>> pivotalMap = new HashMap<>();
        for (Map<String, String> record : result) {
            String value = record.remove("value");
            String dimension = record.remove(dimensionToTranspose);
            Map<String, String> thirdDimensionParams = thirdDimension.stream()
                    .collect(
                            HashMap::new,
                            (map, param) -> map.put(param, record.get(param)),
                            HashMap::putAll
                    );
            thirdDimension.forEach(record::remove);
            pivotalMap.computeIfAbsent(thirdDimensionParams, whatever -> new StrictHashMap<>()).computeIfAbsent(record, whatever -> new StrictHashMap<>()).put(dimension, value);
        }
        // merge key and value maps into one
        List<Map<String, String>> cast = new ArrayList<>();
        for (Map.Entry<Map<String, String>, Map<Map<String, String>, Map<String, String>>> entry : pivotalMap.entrySet()) {
            for (Map.Entry<Map<String, String>, Map<String, String>> inside : entry.getValue().entrySet()) {
                Map<String, String> firstDimension = inside.getKey();
                firstDimension.putAll(inside.getValue());
                firstDimension.putAll(entry.getKey());
                cast.add(firstDimension);
            }
        }
        return cast;
    }
}
