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

package org.metastringfoundation.healthheatmap.dataset.table.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.metastringfoundation.healthheatmap.dataset.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.dataset.table.Table;
import org.metastringfoundation.healthheatmap.helpers.FileManager;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CSVTable implements Table {
    private List<CSVRecord> records;
    private int totalRecords;
    private int eachRecordSize;

    public static CSVTable fromPath(String path) throws DatasetIntegrityError {
        return new CSVTable(FileManager.getPathFromString(path));
    }

    public CSVTable(Path path) throws DatasetIntegrityError {
        try {
            Reader csvReader = FileManager.getFileReader(path);
            CSVParser csvParser = new CSVParser(csvReader, CSVFormat.DEFAULT);
            setRecords(csvParser.getRecords());
        } catch (Exception e) {
            throw new DatasetIntegrityError(e);
        }
    }

    public CSVTable(String csvString) throws DatasetIntegrityError {
        try {
            CSVParser csvParser = CSVParser.parse(csvString, CSVFormat.DEFAULT);
            setRecords(csvParser.getRecords());
        } catch (IOException e) {
            throw new DatasetIntegrityError(e);
        }
    }

    private void setRecords(List<CSVRecord> recordsRead) throws DatasetIntegrityError {
        records = recordsRead;
        validateRecords();
        calculateParams();
    }

    private void validateRecords() throws DatasetIntegrityError {
        boolean nonEmpty = atLeastOneRecord();
        boolean smoothEdge = checkRecordsEqualSize();

        if (!(nonEmpty && smoothEdge)) {
            throw new DatasetIntegrityError("There should be more than 0 records and all of equal size");
        }
    }

    private boolean atLeastOneRecord() {
        return records.size() > 0;
    }

    private boolean checkRecordsEqualSize() {
        int sizeOfFirstRecord = records.get(0).size();
        for (CSVRecord record: records) {
            if (record.size() != sizeOfFirstRecord) {
                return false;
            }
        }
        return true;
    }

    private void calculateParams() {
        calculateRecordSizes();
    }

    private void calculateRecordSizes() {
        // We verify that all records are equal size during validation
        eachRecordSize = records.get(0).size();

        totalRecords = records.size();
    }

    @Override
    public List<List<String>> getTable() {
        List<List<String>> table = new ArrayList<>();
        for (int record = 0; record < totalRecords; record++) {
            table.add(getRow(record));
        }
        return table;
    }

    @Override
    public List<String> getRow(int rowNumber) {
        List<String> row = new ArrayList<>();
        records.get(rowNumber).forEach(row::add);
        return row;
    }

    @Override
    public List<String> getColumn(int columnNumber) {
        List<String> column = new ArrayList<>();
        records.forEach(record -> column.add(record.get(columnNumber)));
        return column;
    }

    @Override
    public int getNumberOfRows() {
        return totalRecords;
    }

    @Override
    public int getNumberOfColumns() {
        return eachRecordSize;
    }
}
