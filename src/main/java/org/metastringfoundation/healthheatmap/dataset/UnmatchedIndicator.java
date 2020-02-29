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

package org.metastringfoundation.healthheatmap.dataset;

import java.util.HashMap;
import java.util.Map;

import static org.metastringfoundation.healthheatmap.helpers.PatternParsingAssistants.quotedDimension;

public class UnmatchedIndicator {
    private String name;

    public static Map<Integer, UnmatchedIndicator> getIndicator(Map<Integer, Map<String, String>> dimensionsMap) {
        Map<Integer, UnmatchedIndicator> result = new HashMap<>();
        for (Map.Entry<Integer, Map<String, String>> row: dimensionsMap.entrySet()) {
            String indicatorName = row.getValue().get(quotedDimension("indicator"));
            if (!(indicatorName == null)) {
                UnmatchedIndicator indicator = new UnmatchedIndicator();
                indicator.setName(indicatorName);
                result.put(row.getKey(), indicator);
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
