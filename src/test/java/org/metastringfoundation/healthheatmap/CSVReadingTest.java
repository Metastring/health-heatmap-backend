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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.dataset.CSVRangeDescription;
import org.metastringfoundation.healthheatmap.dataset.CSVTableDescription;
import org.metastringfoundation.healthheatmap.dataset.TableRangeReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CSVReadingTest {

    @Test
    void correctlyDeserializes() throws JsonProcessingException {
        CSVTableDescription tableDescription = new CSVTableDescription();

        List<CSVRangeDescription> rangeDescriptionList = new ArrayList<>();
        CSVRangeDescription range1 = new CSVRangeDescription();
        range1.setPattern("something");
        range1.setRange(new TableRangeReference("A1:B2"));
        rangeDescriptionList.add(range1);

        tableDescription.setRangeDescriptionList(rangeDescriptionList);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(tableDescription);

        String expectedJson = "{\"rangeDescriptionList\":[{\"range\":{\"startingCell\":{\"row\":1,\"column\":0},\"endingCell\":{\"row\":2,\"column\":1}},\"pattern\":\"something\"}]}";
        assertEquals(expectedJson, json);
    }

    @Test()
    void correctlyReadsJSON() throws IOException {
        String jsonFileName = "ahs.test.json";
        String json = IOUtils.toString(
                this.getClass().getResourceAsStream(jsonFileName),
                StandardCharsets.UTF_8
        );
        ObjectMapper mapper = new ObjectMapper();
        CSVTableDescription description = (CSVTableDescription) mapper.readValue(json, CSVTableDescription.class);

        System.out.println(json);

        assertEquals(1, 1);
    }

}