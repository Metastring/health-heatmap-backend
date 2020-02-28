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

import org.apache.commons.csv.CSVRecord;
import org.metastringfoundation.healthheatmap.datapoint.DataPoint;
import org.metastringfoundation.healthheatmap.datapoint.FloatDataPoint;
import org.metastringfoundation.healthheatmap.datapoint.StringDataPoint;
import org.metastringfoundation.healthheatmap.helpers.ListUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVDataset {
    private Path path;
    private TableRangeReference indicatorInfo;
    private TableRangeReference entityInfo;
    private TableRangeReference dataInfo;
    private DatasetMetadata metadata;

    private CSVTable csvReader;
    private List<CSVRecord> records;

    private List<String> indicators;
    private List<String> entities;
    private List<List<DataPoint>> dataGroupedByEntities;

    public static class builder {
        private Path path;
        private TableRangeReference indicatorInfo = new TableRangeReference("B0:0");
        private TableRangeReference entityInfo = new TableRangeReference("A1:A");
        private TableRangeReference dataInfo = new TableRangeReference("B1: "); //TODO avoid the space hack on part two of the reference
        private DatasetMetadata metadata = new DatasetMetadata("Untitled");

        public builder path(String path) {
            this.path = Paths.get(path);
            return this;
        }

        public builder indicator(String rangeReference) throws DatasetIntegrityError {
            TableRangeReference indicatorInfo = new TableRangeReference(rangeReference);
            if (indicatorInfo.getRangeType() == TableRangeReference.RangeType.ROW_AND_COLUMN) {
                throw new DatasetIntegrityError("Multi-dimensional indicator array is not yet supported");
            }
            this.indicatorInfo = indicatorInfo;
            return this;
        }

        public builder entity(String rangeReference) throws  DatasetIntegrityError {
            TableRangeReference entityInfo = new TableRangeReference(rangeReference);
            if (entityInfo.getRangeType() == TableRangeReference.RangeType.ROW_AND_COLUMN) {
                throw new DatasetIntegrityError("Multi-dimensional entity array is not yet supported");
            }
            this.entityInfo = entityInfo;
            return this;
        }

        public builder data(String rangeReference) {
            this.dataInfo = new TableRangeReference(rangeReference);
            return this;
        }

        public builder ranges(String singleRangeReference) throws DatasetIntegrityError {
            String[] ranges = singleRangeReference.split(",");
            return this.indicator(ranges[0])
                    .entity(ranges[1])
                    .data(ranges[2]);
        }

        public builder metadata(DatasetMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public CSVDataset build() throws DatasetIntegrityError {
            if (this.path == null) {
                throw new DatasetIntegrityError("Path to CSV must be set with builder");
            }
            return new CSVDataset(this);
        }
    }

    private CSVDataset(builder b) throws DatasetIntegrityError {
        if (b.entityInfo.getRangeType() == b.indicatorInfo.getRangeType()) {
            throw new DatasetIntegrityError("Cannot have both indicators and entities in same dimension");
        }

        this.path = b.path;
        this.indicatorInfo = b.indicatorInfo;
        this.entityInfo = b.entityInfo;
        this.dataInfo = b.dataInfo;
        this.metadata = b.metadata;
        parseData();
    }

    public void parseData() throws DatasetIntegrityError {
        this.csvReader = new CSVTable(path);
    }

    private void calculateIndicators() throws DatasetIntegrityError {
        try {
            this.indicators = CSVTableUtils.dereferenceOneDimension(this.indicatorInfo, this.records);
        } catch (IndexOutOfBoundsException e) {
            throw new DatasetIntegrityError("No indicators found in file");
        }
    }

    public List<String> getIndicators() {
        return this.indicators;
    }


    private void calculateEntities() throws DatasetIntegrityError {
        try {
            this.entities = CSVTableUtils.dereferenceOneDimension(this.entityInfo, this.records);
        } catch (IndexOutOfBoundsException e) {
            throw new DatasetIntegrityError("No entities found in file");
        }
    }

    public List<String> getEntities() {
        return this.entities;
    }

     private void calculateDataGroupedByEntities() throws DatasetIntegrityError {
        boolean needsTransposition = false;
        if (this.entityInfo.getRangeType() == TableRangeReference.RangeType.ROW_ONLY) {
            // entity info is written in a row. Which means entities are in the X-dimension. We need to transpose data.
            needsTransposition = true;
        }

        List<List<String>> stringDataGroupedByEntities = CSVTableUtils.dereferenceTable(this.dataInfo, this.records);
        //TODO transpose this data if required
         if (needsTransposition) {
             stringDataGroupedByEntities = ListUtils.transpose(stringDataGroupedByEntities);
         }

        List<List<DataPoint>> dataGroupedByEntities = new ArrayList<>();

        for (List<String> rowBeingRead: stringDataGroupedByEntities) {
            List<DataPoint> dataOfThisRow = new ArrayList<>();
            for (int column = 0; column < rowBeingRead.size(); column++) {
                String unparsedDataPoint = rowBeingRead.get(column);
                DataPoint parsedDataPoint = getDataPointFromString(unparsedDataPoint);
                dataOfThisRow.add(parsedDataPoint);
            }
            dataGroupedByEntities.add(dataOfThisRow);
        }

        if (dataGroupedByEntities.size() == 0) {
            throw new DatasetIntegrityError("No data rows found at all");
        }

         this.dataGroupedByEntities = dataGroupedByEntities;
     }

    public List<List<DataPoint>> getDataGroupedByEntities() {
        return this.dataGroupedByEntities;
    }

    public DatasetMetadata getMetadata() {
        return this.metadata;
    }

    private DataPoint getDataPointFromString(String unparsedDataPoint) {
        return getStringDataPoint(unparsedDataPoint);
    }

    private DataPoint intelligentDataPoint(String unparsedDataPoint) {
        try {
            Float floatDataPoint = Float.parseFloat(unparsedDataPoint);
            return new FloatDataPoint(floatDataPoint);
        } catch (NumberFormatException e) {
            return new StringDataPoint(unparsedDataPoint);
        }
    }

    private StringDataPoint getStringDataPoint(String unparsedDataPoint) {
        return new StringDataPoint(unparsedDataPoint);
    }
}