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

import java.util.Objects;

public class TableCell {
    private int row;
    private int column;
    private String value;

    public TableCell(int row, int column, String value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int get(String dimension) {
        if (dimension.equals("row")) return getRow();
        if (dimension.equals("column")) return getColumn();
        throw new IllegalArgumentException("Either a row or a column dimension can be got");
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TableCell other = (TableCell) obj;
        return (other.getColumn() == this.getColumn() &&
                other.getRow() == this.getRow() &&
                other.getValue().equals(this.getValue())
        );
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                "column: " + getColumn() + "\n" +
                "row: " + getRow() + "\n"  +
                "value: " + getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column, value);
    }
}
