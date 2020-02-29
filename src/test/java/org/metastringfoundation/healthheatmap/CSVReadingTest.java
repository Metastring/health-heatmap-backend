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
import org.metastringfoundation.healthheatmap.dataset.*;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
    void endToEndTest() throws IOException, DatasetIntegrityError {
        String csv = IOUtils.toString(
                this.getClass().getResourceAsStream("sampleData.csv"),
                StandardCharsets.UTF_8
        );
        String csvPath = this.getClass().getResource("sampleData.csv").getPath();
        String descriptionPath = this.getClass().getResource("sampleData.description.json").getPath();

        CSVTable table = CSVTable.fromPath(csvPath);
        CSVTableDescription tableDescription = CSVTableDescription.fromPath(descriptionPath);

        Dataset csvDataset = new CSVTableToDatasetAdapter(table, tableDescription);
        Collection<UnmatchedDataElement> elements = csvDataset.getData();

        Collection<UnmatchedDataElement> expectedElements = new HashSet<>();

        UnmatchedGeography kannur = new UnmatchedGeography();
        kannur.setState("Kerala");
        kannur.setDistrict("Kannur");

        UnmatchedGeography bangalore = new UnmatchedGeography();
        bangalore.setState("Karnataka");
        bangalore.setDistrict("Bangalore");

        UnmatchedIndicator mmr = new UnmatchedIndicator();
        mmr.setName("MMR");

        UnmatchedIndicator u5mr = new UnmatchedIndicator();
        u5mr.setName("U5MR");

        UnmatchedDataElement d1 = new UnmatchedDataElement();
        d1.setGeography(kannur);
        d1.setIndicator(mmr);
        d1.setValue("0.5");
        expectedElements.add(d1);

        UnmatchedDataElement d2 = new UnmatchedDataElement();
        d2.setGeography(kannur);
        d2.setIndicator(u5mr);
        d2.setValue("0.6");
        expectedElements.add(d2);

        UnmatchedDataElement d3 = new UnmatchedDataElement();
        d3.setGeography(bangalore);
        d3.setIndicator(mmr);
        d3.setValue("1");
        expectedElements.add(d3);

        UnmatchedDataElement d4 = new UnmatchedDataElement();
        d4.setGeography(bangalore);
        d4.setIndicator(u5mr);
        d4.setValue("1.2");
        expectedElements.add(d4);

        assertEquals(expectedElements, elements);
    }
}