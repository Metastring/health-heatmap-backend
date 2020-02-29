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

import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.dataset.CSVCell;
import org.metastringfoundation.healthheatmap.dataset.CSVTable;
import org.metastringfoundation.healthheatmap.dataset.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.dataset.TableRangeReference;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableTest {

    @Test
    public void getRangeGetsRange() throws DatasetIntegrityError {
        String simpleSample = this.getClass().getResource("sampleData.csv").getPath();
        CSVTable table = CSVTable.fromPath(simpleSample);
        List<CSVCell> obtainedRange = table.getRange(new TableRangeReference("C2:D3"));

        List<CSVCell> expectedRange = new ArrayList<>();
        expectedRange.add(new CSVCell(1, 2, "0.5"));
        expectedRange.add(new CSVCell(1, 3, "0.6"));
        expectedRange.add(new CSVCell(2, 2, "1"));
        expectedRange.add(new CSVCell(2, 3, "1.2"));

        assertEquals(expectedRange, obtainedRange);
    }
}
