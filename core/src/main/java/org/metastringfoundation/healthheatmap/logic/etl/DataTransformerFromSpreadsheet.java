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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.healthheatmap.helpers.UnknownValueException;
import org.metastringfoundation.healthheatmap.logic.DataTransformer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DataTransformerFromSpreadsheet implements DataTransformer {
    private final Map<Map<String, String>, List<Map<String, String>>> rules;
    private final List<String> keyRawHeaders;
    private final List<String> keyHeaders;
    private final List<String> valueHeaders;
    private final Set<Map<String, String>> lookupFailureKeys = new LinkedHashSet<>();

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

    private Map<Map<String, String>, List<Map<String, String>>> parseCSVToRules(CSVParser csvParser) throws IOException {
        return csvParser.getRecords().stream()
                .collect(Collectors.groupingBy(
                        this::getRuleKey,
                        Collectors.mapping(this::getRuleValue, Collectors.toList())
                        )
                );
    }

    private Map<String, String> getRuleKey(CSVRecord record) {
        return keyRawHeaders.stream()
                .collect(Collectors.toMap(this::removeMatchPrefix, r -> record.get(r).toLowerCase()));
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
    public List<DataPoint> transform(DataPoint data) throws UnknownValueException {
        Map<String, String> lookupKey = lookupKeyExtract(data);
        Optional<List<Map<String, String>>> lookupValue = rulesLookup(lookupKey);
        if (lookupValue.isPresent()) {
            return lookupValue.get().stream()
                    .map(valuesToAdd -> {
                        DataPoint clone = DataPoint.from(data);
                        clone.putAll(valuesToAdd);
                        return clone;
                    })
                    .collect(Collectors.toList());
        } else {
            lookupFailureKeys.add(lookupKey);
            throw new UnknownValueException("Cannot find key");
        }
    }

    @Override
    public List<Map<String, String>> getUnmatchedKeysFound() {
        return new ArrayList<>(lookupFailureKeys);
    }

    @Override
    public Map<String, String> getKeyApplicable(DataPoint data) {
        return lookupKeyExtract(data);
    }

    @Override
    public Map<Map<String, String>, List<Map<String, String>>> getRules() {
        return rules;
    }

    private Optional<List<Map<String, String>>> rulesLookup(Map<String, String> key) {
        return Optional.ofNullable(rules.get(key));
    }

    private <T extends Map<String, String>> Map<String, String> lookupKeyExtract(T data) {
        return keyHeaders.stream()
                .collect(Collectors.toMap(k -> k, k -> data.getOrDefault(k, "").toLowerCase().strip()));
    }
}
