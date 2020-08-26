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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadCSVAsMap {
    public static List<Map<String, String>> get(Path path) throws IOException {
        return get(FileManager.readFileAsString(path));
    }

    public static List<Map<String, String>> get(String csv) throws IOException {
        CSVParser csvParser = CSVParser.parse(csv, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        return csvParser.getRecords().stream()
                .map(CSVRecord::toMap)
                .collect(Collectors.toList());
    }
}
