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

import org.metastringfoundation.data.DataPoint;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataTransformerForDates implements DataTransformer {
    @Override
    public List<DataPoint> transform(DataPoint data) {
        if (data.containsKey("duration.start")) {
            data.put("duration.start", cleanDate(data.get("duration.start")));
        }
        if (data.containsKey("duration.end")) {
            data.put("duration.end", cleanDate(data.get("duration.end")));
        }
        return List.of(data);
    }

    private String cleanDate(String s) {
        try {
            String day;
            String month;
            String year;
            List<String> split = Arrays.asList(s.split("[-.]"));
            if (split.get(2).length() == 2 && split.get(0).length() < 3) {
                year = "20" + split.get(2);
                day = split.get(0);
            } else if (split.get(2).length() == 4) {
                year = split.get(2);
                day = split.get(0);
            } else if (split.get(0).length() == 4) {
                year = split.get(0);
                day = split.get(2);
            } else {
                throw new IllegalArgumentException("What is this date: " + s);
            }
            month = split.get(1);
            return String.join("-", List.of(year, month, day));
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            return s;
        }

    }

    @Override
    public Set<Map<String, String>> getUnmatchedKeysFound() {
        return Set.of();
    }
}
