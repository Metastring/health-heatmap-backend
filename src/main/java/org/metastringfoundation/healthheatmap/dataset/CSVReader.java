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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

public class CSVReader {
    private String path;

    public CSVReader(String path) {
        this.path = path;
    }

    private Reader getFileReader(String path) throws DatasetIntegrityError {
        try {
            return new FileReader(path);
        } catch (Exception e) {
            throw new DatasetIntegrityError(e);
        }
    }

    private List<CSVRecord> getCSVRecords(Reader in) throws DatasetIntegrityError {
        try {
            CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT);
            return parser.getRecords();
        } catch (IOException e) {
            throw new DatasetIntegrityError(e);
        }
    }

    public List<CSVRecord> getCSVRecords() throws DatasetIntegrityError {
        Reader in = getFileReader(path);
        return getCSVRecords(in);
    }

    public void readCSV() throws DatasetIntegrityError {
        List<CSVRecord> list = getCSVRecords();
        for (CSVRecord record: list) {
            Iterator recordIterator = record.iterator();
            while (recordIterator.hasNext()) {
                System.out.println(recordIterator.next());
            }
        }
    }
}
