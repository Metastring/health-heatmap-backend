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

package org.metastringfoundation.healthheatmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.dataset.CSVRangeDescription;
import org.metastringfoundation.healthheatmap.dataset.CSVTableDescription;
import org.metastringfoundation.healthheatmap.dataset.TableRangeReference;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CSVReadingTest {

    @Test
    void correctlySerializesDescription() throws JsonProcessingException {
        CSVTableDescription tableDescription = new CSVTableDescription();

        List<CSVRangeDescription> rangeDescriptionList = new ArrayList<>();
        CSVRangeDescription range1 = new CSVRangeDescription();
        range1.setPattern("#{indicator}");
        range1.setRange(new TableRangeReference("A1:B2"));
        rangeDescriptionList.add(range1);

        tableDescription.setRangeDescriptionList(rangeDescriptionList);

        String json = Jsonizer.asJSON(tableDescription);

        String expectedJson = "{\"rangeDescriptionList\":[{\"range\":{\"startingCell\":{\"row\":0,\"column\":0},\"endingCell\":{\"row\":1,\"column\":1}},\"pattern\":\"#{indicator}\"}]}";
        assertEquals(expectedJson, json);
    }

    @Test()
    void correctlyReadsJSON() throws IOException {
        String jsonFileName = "ahs.test.json";
        String path = this.getClass().getResource(jsonFileName).getPath();
        CSVTableDescription description = CSVTableDescription.fromPath(path);

        CSVRangeDescription range = new CSVRangeDescription();
        range.setRange(new TableRangeReference("A1:B2"));
        range.setPattern("#{indicator}");
        List<CSVRangeDescription> rangeDescriptionList = new ArrayList<>();
        rangeDescriptionList.add(range);

        CSVTableDescription expectedDescription = new CSVTableDescription();
        expectedDescription.setRangeDescriptionList(rangeDescriptionList);

        assertEquals(expectedDescription, description);
    }

    @Test
    void endToEndTest() throws IOException {
        String csv = IOUtils.toString(
                this.getClass().getResourceAsStream("sampleData.csv"),
                StandardCharsets.UTF_8
        );
        String csvPath = this.getClass().getResource("sampleData.csv").getPath();

    }

}