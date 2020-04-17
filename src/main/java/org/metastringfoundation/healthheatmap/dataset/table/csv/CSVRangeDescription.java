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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.metastringfoundation.healthheatmap.dataset.table.TableRangeReference;

import java.util.List;
import java.util.Map;

public class CSVRangeDescription {
    private TableRangeReference range;
    private String pattern;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> replacements;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> metadata;

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

    public List<String> getReplacements() {
        return replacements;
    }

    public void setReplacements(List<String> replacements) {
        this.replacements = replacements;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CSVRangeDescription other = (CSVRangeDescription) obj;
        return (other.getRange().equals(this.getRange())
                && other.getPattern().equals(this.getPattern())
        );
    }

    @Override
    public String toString() {
        return "CSVRangeDescription{" +
                "range=" + range +
                ", pattern='" + pattern + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
