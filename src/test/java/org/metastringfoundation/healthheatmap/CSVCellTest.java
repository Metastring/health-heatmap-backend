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

import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVCellTest {
    @Test
    public void equalsAreEquals() {
        CSVCell cell1 = new CSVCell(1, 1, "1");
        CSVCell cell2 = new CSVCell(1, 1, "1");
        assertEquals(cell1, cell2);

        Collection<CSVCell> set1 = new HashSet<>();
        set1.add(cell1);
        set1.add(cell2);

        Collection<CSVCell> set2 = new HashSet<>();
        set2.add(cell2);
        set2.add(cell1);

        assertEquals(set1, set2);
    }
}
