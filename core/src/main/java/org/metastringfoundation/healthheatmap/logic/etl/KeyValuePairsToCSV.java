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

import com.google.common.collect.Iterables;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.metastringfoundation.healthheatmap.helpers.ListAndMapUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyValuePairsToCSV {
    private final List<Map<String, String>> inputMaps;
    private final List<String> headers;
    private final CSVPrinter csvPrinter;
    private final StringWriter stringWriter = new StringWriter();

    public KeyValuePairsToCSV(List<Map<String, String>> inputMaps, List<String> headers) throws IOException {
        this.inputMaps = inputMaps;
        this.headers = headers;
        csvPrinter = new CSVPrinter(
                stringWriter,
                CSVFormat.DEFAULT.withHeader(Iterables.toArray(headers, String.class)).withSystemRecordSeparator()
        );
        convert();
    }

    private void convert() throws IOException {
        for (Map<String, String> record : inputMaps) {
            csvPrinter.printRecord(getPrintableRecord(record));
        }
    }

    private List<String> getPrintableRecord(Map<String, String> input) {
        return headers.stream()
                .map(input::get)
                .collect(Collectors.toList());
    }

    public String getCSV() throws IOException {
        csvPrinter.flush();
        return stringWriter.toString();
    }

    public static <T> String convertToCSVWithFirstElementKeysAsHeaders(List<Map<String, T>> inputMaps) throws IOException {
        if (inputMaps.size() < 1) {
            throw new IllegalArgumentException("At least one item must be present in the input");
        }
        List<String> headers = new ArrayList<>(inputMaps.get(0).keySet());
        List<Map<String, String>> stringOnlyMaps = ListAndMapUtils.getListOfStringOnlyMaps(inputMaps);
        return new KeyValuePairsToCSV(stringOnlyMaps, headers).getCSV();
    }


}
