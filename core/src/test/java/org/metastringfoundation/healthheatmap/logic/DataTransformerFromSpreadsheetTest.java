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
import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.healthheatmap.helpers.UnknownValueException;
import org.metastringfoundation.healthheatmap.logic.etl.DataTransformerFromSpreadsheet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataTransformerFromSpreadsheetTest {

    @Test
    public void shouldTransformCorrectly() throws IOException, UnknownValueException {
        String spreadsheet = "match source,match indicator,newSource,newIndicator\nNFHS4,MMRate,NFHS-4,MMR\nNFHS4,MMRate,NFHS-4,MMRatio\n";
        DataTransformer transformer = new DataTransformerFromSpreadsheet(spreadsheet);
        Map<String, String> data = Map.of("source", "NFHS4", "indicator", "MMRate");
        DataPoint dataPoint = DataPoint.from(data);
        List<DataPoint> expected = List.of(
                DataPoint.from(Map.of(
                        "source", "NFHS4",
                        "indicator", "MMRate",
                        "newSource", "NFHS-4",
                        "newIndicator", "MMR")),
                DataPoint.from(Map.of(
                        "source", "NFHS4",
                        "indicator", "MMRate",
                        "newSource", "NFHS-4",
                        "newIndicator", "MMRatio"))
        );
        List<DataPoint> actual = transformer.transform(dataPoint);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldCollectKeyLookupFailuresCorrectly() throws IOException {
        var spreadsheet = "match source,match indicator,source,indicator\nNFHS4,MMRate,NFHS-4,MMR\n";
        var transformer = new DataTransformerFromSpreadsheet(spreadsheet);
        var data = List.of(
                DataPoint.from(Map.of("source", "NFHS4", "indicator", "MMRate")),
                DataPoint.from(Map.of("source", "NFHS-4", "indicator", "MMRate"))
        );
        var expected = List.of(Map.of(
                "source", "NFHS-4",
                "indicator", "MMRate"
        ));
        for (DataPoint datum : data) {
            try {
                transformer.transform(datum);
            } catch (UnknownValueException ee) {
                // expected exception
            }
        }
        var actual = transformer.getUnmatchedKeysFound();
        assertEquals(expected, actual);
    }
}