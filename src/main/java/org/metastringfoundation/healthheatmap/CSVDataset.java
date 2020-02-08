package org.metastringfoundation.healthheatmap;

import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CSVDataset implements Dataset {
    private String path;
    private CSVDatasetRangeReference indicatorInfo;
    private CSVDatasetRangeReference entityInfo;
    private CSVDatasetRangeReference dataInfo;
    private DatasetMetadata metadata;

    private CSVReader csvReader;
    private List<CSVRecord> records;

    private List<String> indicators;
    private List<String> entities;
    private List<List<DataPoint>> dataGroupedByEntities;

    public static class builder {
        private String path;
        private CSVDatasetRangeReference indicatorInfo = new CSVDatasetRangeReference("B0:0");
        private CSVDatasetRangeReference entityInfo = new CSVDatasetRangeReference("A1:A");
        private CSVDatasetRangeReference dataInfo = new CSVDatasetRangeReference("B1: "); //TODO avoid the space hack on part two of the reference
        private DatasetMetadata metadata = new DatasetMetadata("Untitled");

        public builder path(String path) {
            this.path = path;
            return this;
        }

        public builder indicator(String rangeReference) throws DatasetIntegrityError {
            CSVDatasetRangeReference indicatorInfo = new CSVDatasetRangeReference(rangeReference);
            if (indicatorInfo.getRangeType() == CSVDatasetRangeReference.RangeType.ROW_AND_COLUMN) {
                throw new DatasetIntegrityError("Multi-dimensional indicator array is not yet supported");
            }
            this.indicatorInfo = indicatorInfo;
            return this;
        }

        public builder entity(String rangeReference) throws  DatasetIntegrityError {
            CSVDatasetRangeReference entityInfo = new CSVDatasetRangeReference(rangeReference);
            if (entityInfo.getRangeType() == CSVDatasetRangeReference.RangeType.ROW_AND_COLUMN) {
                throw new DatasetIntegrityError("Multi-dimensional entity array is not yet supported");
            }
            this.entityInfo = entityInfo;
            return this;
        }

        public builder data(String rangeReference) {
            this.dataInfo = new CSVDatasetRangeReference(rangeReference);
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
        this.csvReader = new CSVReader(path);
        this.records = csvReader.getCSVRecords();
        calculateIndicators();
        System.out.println("Found these indicators " + this.indicators.toString());
        calculateEntities();
        System.out.println("Found these entities " + this.entities.toString());
        calculateDataGroupedByEntities();
    }

    private void calculateIndicators() throws DatasetIntegrityError {
        try {
            this.indicators = CSVDatasetRangeReference.dereferenceOneDimension(this.indicatorInfo, this.records);
        } catch (IndexOutOfBoundsException e) {
            throw new DatasetIntegrityError("No indicators found in file");
        }
    }

    @Override
    public List<String> getIndicators() {
        return this.indicators;
    }


    private void calculateEntities() throws DatasetIntegrityError {
        try {
            this.entities = CSVDatasetRangeReference.dereferenceOneDimension(this.entityInfo, this.records);
        } catch (IndexOutOfBoundsException e) {
            throw new DatasetIntegrityError("No entities found in file");
        }
    }

    @Override
    public List<String> getEntities() {
        return this.entities;
    }

     private void calculateDataGroupedByEntities() throws DatasetIntegrityError {
        boolean needsTransposition = false;
        if (this.entityInfo.getRangeType() == CSVDatasetRangeReference.RangeType.ROW_ONLY) {
            // entity info is written in a row. Which means entities are in the X-dimension. We need to transpose data.
            needsTransposition = true;
        }

        List<List<String>> stringDataGroupedByEntities = CSVDatasetRangeReference.dereferenceTable(this.dataInfo, this.records);
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

    @Override
    public List<List<DataPoint>> getDataGroupedByEntities() {
        return this.dataGroupedByEntities;
    }

    @Override
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