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

package org.metastringfoundation.healthheatmap.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = TableRangeReferenceDeserializer.class)
public class TableRangeReference {
    private TableCellReference startingCell;
    private TableCellReference endingCell;

    public enum RangeType {
        SINGLE_CELL,
        ROW_ONLY,
        COLUMN_ONLY,
        ROW_AND_COLUMN
    }

    public TableRangeReference(String reference) {
        setReferencesFromString(reference);
    }

    public TableRangeReference() {
    }

    public TableRangeReference(TableCellReference startingCell, TableCellReference endingCell) {
        this.startingCell = startingCell;
        this.endingCell = endingCell;
    }

    public void setStartingCell(TableCellReference startingCell) {
        this.startingCell = startingCell;
    }

    public void setEndingCell(TableCellReference endingCell) {
        this.endingCell = endingCell;
    }

    private void setReferencesFromString(String reference) {
        if (reference.endsWith(":")) {
            reference = reference.concat(" ");
        }
        String[] referenceSplit = reference.split(":");
        if (referenceSplit.length != 2) {
            throw new IllegalArgumentException("Reference should be of format CELL:CELL");
        }
        this.startingCell = new TableCellReference(referenceSplit[0]);
        this.endingCell = new TableCellReference(referenceSplit[1]);
    }

    public static RangeType getRangeType(TableCellReference rangeStartCell, TableCellReference rangeEndCell) {
        int startRow = rangeStartCell.getRow();
        int startColumn = rangeStartCell.getColumn();
        int endRow = rangeEndCell.getRow();
        int endColumn = rangeEndCell.getColumn();


        RangeType rangeType;

        if (startColumn == endColumn && startRow == endRow) {
            rangeType = RangeType.SINGLE_CELL;
        } else if (endColumn > startColumn && endRow > startRow) {
            rangeType = RangeType.ROW_AND_COLUMN;
        } else if (endColumn == startColumn && endRow > startRow) {
            rangeType = RangeType.COLUMN_ONLY;
        } else if (endColumn > startColumn && endRow == startRow) {
            rangeType = RangeType.ROW_ONLY;
        } else // (endRow < startRow || endColumn < startColumn)
        {
            throw new IllegalArgumentException("The starting cell should come before ending cell");
        }
        return rangeType;
    }

    public TableCellReference getStartingCell() {
        return this.startingCell;
    }

    public TableCellReference getEndingCell() {
        return this.endingCell;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TableRangeReference other = (TableRangeReference) obj;
        return (other.getStartingCell().equals(this.getStartingCell())
                && other.getEndingCell().equals(this.getEndingCell())
        );
    }
}
