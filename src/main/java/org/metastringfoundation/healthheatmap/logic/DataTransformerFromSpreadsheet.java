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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataTransformerFromSpreadsheet implements DataTransformer {
    private final Map<Map<String, String>, Map<String, String>> rules;
    private final List<String> keyRawHeaders;
    private final List<String> keyHeaders;
    private final List<String> valueHeaders;

    public DataTransformerFromSpreadsheet(String spreadsheet) throws IOException {
        CSVParser csvParser = CSVParser.parse(spreadsheet, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        keyRawHeaders = getKeyRawHeaders(csvParser);
        keyHeaders = getKeyHeaders();
        valueHeaders = getValueHeaders(csvParser);
        rules = parseCSVToRules(csvParser);
    }

    public static DataTransformerFromSpreadsheet getDataTransformerCrashingOnError(String spreadsheet) {
        try {
            return new DataTransformerFromSpreadsheet(spreadsheet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getKeyRawHeaders(CSVParser csvParser) {
        return csvParser.getHeaderNames().stream()
                .filter(header -> header.startsWith("match "))
                .collect(Collectors.toList());
    }


    private List<String> getKeyHeaders() {
        return keyRawHeaders.stream()
                .map(this::removeMatchPrefix)
                .collect(Collectors.toList());
    }

    private List<String> getValueHeaders(CSVParser csvParser) {
        return csvParser.getHeaderNames().stream()
                .filter(header -> !header.startsWith("match "))
                .collect(Collectors.toList());
    }

    private Map<Map<String, String>, Map<String, String>> parseCSVToRules(CSVParser csvParser) throws IOException {
        return csvParser.getRecords().stream()
                .collect(Collectors.toMap(this::getRuleKey, this::getRuleValue));
    }

    private Map<String, String> getRuleKey(CSVRecord record) {
        return keyRawHeaders.stream()
                .collect(Collectors.toMap(this::removeMatchPrefix, record::get));
    }

    private String removeMatchPrefix(String source) {
        return removePrefix(source, "match ");
    }

    private String removePrefix(String source, String prefix) {
        return source.substring(prefix.length());
    }


    private Map<String, String> getRuleValue(CSVRecord record) {
        return valueHeaders.stream()
                .collect(Collectors.toMap(k -> k, record::get));
    }

    @Override
    public <T extends Map<String, String>> T transform(T data) {
        Map<String, String> valuesToAdd = rulesLookup(data);
        if (valuesToAdd != null) {
            data.putAll(valuesToAdd);
        }
        return data;
    }

    private <T extends Map<String, String>> Map<String, String> rulesLookup(T data) {
        return rules.get(lookupKeyExtract(data));
    }

    private <T extends Map<String, String>> Map<String, String> lookupKeyExtract(T data) {
        return keyHeaders.stream()
                .collect(Collectors.toMap(k -> k, data::get));
    }
}
