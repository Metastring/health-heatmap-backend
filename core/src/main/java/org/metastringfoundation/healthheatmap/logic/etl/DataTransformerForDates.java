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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataTransformerForDates implements DataTransformer {
    @Override
    public List<DataPoint> transform(DataPoint data) {
        if (data.containsKey("duration.start")) {
            data.put("duration.start", cleanDate(data.get("duration.start")));
        }
        if (data.containsKey("duration.end")) {
            data.put("duration.end", cleanDate(data.get("duration.end")));
        }
        data.put("duration.type", getDateType(data));
        return List.of(data);
    }

    private String getDateType(DataPoint data) {
        if (data.containsKey("duration.type")) {
            return data.get("duration.type");
        }
        try {
            String[] startSplit = data.get("duration.start").split("-");
            String[] endSplit = data.get("duration.end").split("-");
            List<Integer> start = Arrays.stream(startSplit).map(Integer::parseInt).collect(Collectors.toList());
            List<Integer> end = Arrays.stream(endSplit).map(Integer::parseInt).collect(Collectors.toList());
            Integer startYear = start.get(0);
            Integer endYear = end.get(0);
            Integer startMonth = start.get(1);
            Integer endMonth = end.get(1);
            Integer startDay = end.get(2);
            Integer endDay = end.get(2);

            if (startYear.equals(endYear) && startMonth.equals(1) && endMonth.equals(12) && startDay.equals(1) && endDay.equals(31)) {
                return "YEARLY";
            }
            if (startYear.equals(endYear) && startMonth.equals(endMonth) && startDay.equals(endDay)) {
                return "DAILY";
            } else {
                return "UNKNOWN";
            }
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String cleanDate(String s) {
        try {
            String day;
            String month;
            String year;
            List<String> split = Arrays.asList(s.split("[-./]"));
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
    public List<Map<String, String>> getUnmatchedKeysFound() {
        return List.of();
    }

    @Override
    public Map<String, String> getKeyApplicable(DataPoint data) {
        return Map.of("duration.start", data.get("duration.start"), "duration.end", data.get("duration.end"));
    }

    @Override
    public Map<Map<String, String>, List<Map<String, String>>> getRules() {
        return Map.of();
    }
}
