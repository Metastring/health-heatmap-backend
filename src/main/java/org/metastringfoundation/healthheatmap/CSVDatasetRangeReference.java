package org.metastringfoundation.healthheatmap;

import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.List;


public class CSVDatasetRangeReference {
    private CSVDatasetCellReference startingCell;
    private CSVDatasetCellReference endingCell;
    private RangeType rangeType;

    public enum RangeType {
        SINGLE_CELL,
        ROW_ONLY,
        COLUMN_ONLY,
        ROW_AND_COLUMN
    }

    CSVDatasetRangeReference(String reference) {
        convertReferenceToCellReferences(reference);
    }

    private void convertReferenceToCellReferences(String reference) {
        String[] referenceSplit = reference.split(":");
        if (referenceSplit.length != 2) {
            throw new IllegalArgumentException("Reference should be of format CELL:CELL");
        }
        this.startingCell = new CSVDatasetCellReference(referenceSplit[0]);
        this.endingCell = new CSVDatasetCellReference(referenceSplit[1]);

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

    public CSVDatasetCellReference getStartingCell() {
        return this.startingCell;
    }

    public CSVDatasetCellReference getEndingCell() {
        return this.endingCell;
    }

    public RangeType getRangeType() {
        return this.rangeType;
    }

    public static List<List<String>> dereferenceTable(CSVDatasetRangeReference rangeReference, List<CSVRecord> records) {
        int startRow = rangeReference.getStartingCell().getRow();
        int startColumn = rangeReference.getStartingCell().getColumn();
        int endRow = rangeReference.getEndingCell().getRow();
        int endColumn = rangeReference.getEndingCell().getColumn();

        List<List<String>> selectedRange = new ArrayList<>();

        for (int row = startRow; row <= endRow && row < records.size(); row++) {
            List<String> currentRowInRange = new ArrayList<>();
            CSVRecord currentRecord = records.get(row);

            for (int column = startColumn; column <= endColumn && column < currentRecord.size(); column++) {
                String thisCell = currentRecord.get(column);
                currentRowInRange.add(thisCell);
            }

            selectedRange.add(currentRowInRange);
        }
        return selectedRange;
    }

    public static List<String> dereferenceOneDimension(CSVDatasetRangeReference rangeReference, List<CSVRecord> records) {
        int startRow = rangeReference.getStartingCell().getRow();
        int startColumn = rangeReference.getStartingCell().getColumn();
        int endRow = rangeReference.getEndingCell().getRow();
        int endColumn = rangeReference.getEndingCell().getColumn();

        if ((endColumn == startColumn) && (endRow == startRow)) {
            throw new IllegalArgumentException("Passed a 2D range reference to a 1D parser");
        }

        List<String> selected1DRange = new ArrayList<>();

        for (int row = startRow; row <= endRow && row < records.size(); row++) {
            CSVRecord currentRecord = records.get(row);

            for (int column = startColumn; column <= endColumn && column < currentRecord.size(); column++) {

                String thisCell = currentRecord.get(column);

                selected1DRange.add(thisCell);
            }
        }
        return selected1DRange;
    }
}
