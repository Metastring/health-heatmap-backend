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

import java.util.List;

public class CSVTableDescription {
    public static class CSVRangeDescription {
        private TableRangeReference range;
        private String pattern;

        public TableRangeReference getRange() {
            return range;
        }

        public void setRange(TableRangeReference range) {
            this.range = range;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

    private List<CSVRangeDescription> rangeDescriptionList;

    public List<CSVRangeDescription> getRangeDescriptionList() {
        return rangeDescriptionList;
    }

    public void setRangeDescriptionList(List<CSVRangeDescription> rangeDescriptionList) {
        this.rangeDescriptionList = rangeDescriptionList;
    }
}
