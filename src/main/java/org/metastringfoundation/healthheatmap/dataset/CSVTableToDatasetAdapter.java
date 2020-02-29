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

import org.metastringfoundation.healthheatmap.pojo.DataElement;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVTableToDatasetAdapter implements Dataset {
    private CSVTable table;
    private CSVTableDescription tableDescription;

    public CSVTableToDatasetAdapter(CSVTable csvTable, CSVTableDescription csvTableDescription) {
        table = csvTable;
        tableDescription = csvTableDescription;
    }

    @Override
    public DatasetMetadata getMetadata() {
        return null;
    }

    @Override
    public Collection<DataElement> getData() {
        Collection<DataElement> dataElements = null;
        for (CSVRangeDescription rangeDescription: tableDescription.getRangeDescriptionList()) {
            Collection<CSVCell> cellsInRange = table.getRange(rangeDescription.getRange());
            String rangePattern = rangeDescription.getPattern();
            Pattern patternParsingPattern = Pattern.compile("/#{([^}]*)}/");
            Matcher rangeMatcher = patternParsingPattern.matcher(rangePattern);
            while (rangeMatcher.find()) {
                rangeMatcher.group();
            }
        }
        return dataElements;
    }
}
