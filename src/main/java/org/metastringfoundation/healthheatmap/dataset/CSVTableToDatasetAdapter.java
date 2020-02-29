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

import java.util.*;

import static org.metastringfoundation.healthheatmap.helpers.PatternParsingAssistants.quotedDimension;

public class CSVTableToDatasetAdapter implements Dataset {
    private CSVTable table;
    private CSVTableDescription tableDescription;

    private static final Map<String, String> regexMapOfDimensions = new HashMap<>();
    static {
        regexMapOfDimensions.put(quotedDimension("entity.district"), ".+");
        regexMapOfDimensions.put(quotedDimension("entity.state"), ".+");
        regexMapOfDimensions.put(quotedDimension("settlement"), ".+");
        regexMapOfDimensions.put(quotedDimension("indicator"), ".+");
        regexMapOfDimensions.put(quotedDimension("data"), ".+");
    }

    public CSVTableToDatasetAdapter(CSVTable csvTable, CSVTableDescription csvTableDescription) {
        table = csvTable;
        tableDescription = csvTableDescription;
    }

    @Override
    public DatasetMetadata getMetadata() {
        return null;
    }

    @Override
    public Collection<UnmatchedDataElement> getData() {
        //TODO: Refactor this function

        Collection<UnmatchedDataElement> dataElements = new HashSet<>();

        // just premature optimization
        String dataDimension = quotedDimension("data");

        // to store temporarily found dimensions
        Map<Integer, Map<String, String>> rowDimensions = new HashMap<>();
        Map<Integer, Map<String, String>> columnDimensions = new HashMap<>();

        // to store temporarily found data
        Collection<UnprocessedDataElement> unprocessedDataElements = new HashSet<>();

        for (CSVRangeDescription rangeDescription: tableDescription.getRangeDescriptionList()) {
            Collection<CSVCell> cellsInRange = table.getRange(rangeDescription.getRange());
            String rangePattern = rangeDescription.getPattern();
            ReversePatternParser patternParser = new ReversePatternParser(rangePattern, regexMapOfDimensions);

            for (CSVCell cell: cellsInRange) {
                int row = cell.getRow();
                int column = cell.getColumn();
                Map<String, String> dimensionsFound = patternParser.parse(cell.getValue());

                if (dimensionsFound.get(dataDimension) != null) {
                    UnprocessedDataElement unprocessedDataElement = new UnprocessedDataElement();
                    unprocessedDataElement.setCsvCell(cell);
                    unprocessedDataElement.setValue(dimensionsFound.get(dataDimension));
                    unprocessedDataElements.add(unprocessedDataElement);
                } else {
                    if (rowDimensions.get(row) == null) {
                        rowDimensions.put(row, dimensionsFound);
                    } else {
                        rowDimensions.get(row).putAll(dimensionsFound);
                    }
                    if (columnDimensions.get(column) == null) {
                        columnDimensions.put(column, dimensionsFound);
                    } else {
                        columnDimensions.get(column).putAll(dimensionsFound);
                    }
                }
            }
        }

        // now let us process the dimensions
        /*
           If a CSV is like this:

               data,data,data,data,key
               data,data,data,data,key
               key, key, key, key, key

           We can see that the columns and rows without data should not have any dimensions associated with it.
         */
        // First, let us find out which columns and rows have data, and remove all dimensions outside this range
        Set<Integer> rowsWithData = new HashSet<>();
        Set<Integer> columnsWithData = new HashSet<>();
        for (UnprocessedDataElement unprocessedDataElement: unprocessedDataElements) {
            rowsWithData.add(unprocessedDataElement.getCsvCell().getRow());
            columnsWithData.add(unprocessedDataElement.getCsvCell().getColumn());
        }
        rowDimensions.keySet().retainAll(rowsWithData);
        columnDimensions.keySet().retainAll(columnsWithData);

        // Now our dimension maps will have only valid dimensions
        // Using that we can find out what indicator, etc they are supposed to contain
        Map<String, String> dimensionsAvailableInDataset = new HashMap<>();

        Map<Integer, UnmatchedGeography> unmatchedEntities = UnmatchedGeography.getGeography(rowDimensions);
        if (!unmatchedEntities.isEmpty()) {
            dimensionsAvailableInDataset.put("entity", "row");
        } else {
            unmatchedEntities = UnmatchedGeography.getGeography(columnDimensions);
        }
        if (!unmatchedEntities.isEmpty()) dimensionsAvailableInDataset.put("entity", "column");

        Map<Integer, UnmatchedIndicator> unmatchedIndicatos = UnmatchedIndicator.getIndicator(rowDimensions);
        if (!unmatchedIndicatos.isEmpty()) {
            dimensionsAvailableInDataset.put("indicator", "row");
        } else {
            unmatchedIndicatos = UnmatchedIndicator.getIndicator(columnDimensions);
        }
        if (!unmatchedIndicatos.isEmpty()) dimensionsAvailableInDataset.put("indicator", "column");


        Map<Integer, UnmatchedSettlement> unmatchedSettlements = UnmatchedSettlement.getSettlement(rowDimensions);
        if (!unmatchedSettlements.isEmpty()) {
            dimensionsAvailableInDataset.put("settlement", "row");
        } else {
            unmatchedSettlements = UnmatchedSettlement.getSettlement(columnDimensions);
        }
        if (!unmatchedSettlements.isEmpty()) dimensionsAvailableInDataset.put("settlement", "column");

        for (UnprocessedDataElement unprocessedDataElement: unprocessedDataElements) {
            UnmatchedDataElement dataElement = new UnmatchedDataElement();

            String geographyDimension = dimensionsAvailableInDataset.get("entity");
            if (geographyDimension != null) {
                dataElement.setGeography(
                        unmatchedEntities.get(
                                unprocessedDataElement.getCsvCell().get(geographyDimension)
                        )
                );
            }

            String indicatorDimension = dimensionsAvailableInDataset.get("indicator");
            if (indicatorDimension != null) {
                dataElement.setIndicator(
                        unmatchedIndicatos.get(
                                unprocessedDataElement.getCsvCell().get(indicatorDimension)
                        )
                );
            }

            String settlementDimension = dimensionsAvailableInDataset.get("settlement");
            if (settlementDimension != null) {
                dataElement.setSettlement(
                        unmatchedSettlements.get(
                                unprocessedDataElement.getCsvCell().get(settlementDimension)
                        )
                );
            }

            dataElement.setValue(unprocessedDataElement.getValue());
            dataElements.add(dataElement);
        }

        return dataElements;
    }
}
