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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataTransformerFromSpreadsheetTest {

    @Test
    public void shouldTransformCorrectly() throws IOException {
        String spreadsheet = "match source,match indicator,newSource,newIndicator\nNFHS4,MMRate,NFHS-4,MMR\n";
        DataTransformer transformer = new DataTransformerFromSpreadsheet(spreadsheet);
        Map<String, String> data = Map.of("source", "NFHS4", "indicator", "MMRate");
        Map<String, String> mutableData = new HashMap<>(data);
        Map<String, String> expected = Map.of(
                "source", "NFHS4",
                "indicator", "MMRate",
                "newSource", "NFHS-4",
                "newIndicator", "MMR");
        Map<String, String> actual = transformer.transform(mutableData);
        assertEquals(expected, actual);
    }
}