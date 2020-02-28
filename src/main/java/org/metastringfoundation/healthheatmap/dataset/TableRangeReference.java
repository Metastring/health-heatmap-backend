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

public class TableRangeReference {
    private TableCellReference startingCell;
    private TableCellReference endingCell;
    private RangeType rangeType;

    public enum RangeType {
        SINGLE_CELL,
        ROW_ONLY,
        COLUMN_ONLY,
        ROW_AND_COLUMN
    }

    TableRangeReference(String reference) {
        convertReferenceToCellReferences(reference);
    }

    private void convertReferenceToCellReferences(String reference) {
        String[] referenceSplit = reference.split(":");
        if (referenceSplit.length != 2) {
            throw new IllegalArgumentException("Reference should be of format CELL:CELL");
        }
        this.startingCell = new TableCellReference(referenceSplit[0]);
        this.endingCell = new TableCellReference(referenceSplit[1]);

        int startRow = this.startingCell.getRow();
        int startColumn = this.startingCell.getColumn();
        int endRow = this.endingCell.getRow();
        int endColumn = this.endingCell.getColumn();


        if (startColumn == endColumn && startRow == endRow) {
            this.rangeType = RangeType.SINGLE_CELL;
        } else if (endColumn > startColumn && endRow > startRow) {
            this.rangeType = RangeType.ROW_AND_COLUMN;
        } else if (endColumn == startColumn && endRow > startRow) {
            this.rangeType = RangeType.COLUMN_ONLY;
        } else if (endColumn > startColumn && endRow == startRow) {
            this.rangeType = RangeType.ROW_ONLY;
        } else // (endRow < startRow || endColumn < startColumn)
        {
            throw new IllegalArgumentException("The starting cell should come before ending cell");
        }
    }

    public TableCellReference getStartingCell() {
        return this.startingCell;
    }

    public TableCellReference getEndingCell() {
        return this.endingCell;
    }

    public RangeType getRangeType() {
        return this.rangeType;
    }

}
