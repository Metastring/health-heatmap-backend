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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metastringfoundation.healthheatmap.helpers.FileManager;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;

import java.io.IOException;
import java.util.List;

public class CSVTableDescription {

    public static CSVTableDescription fromPath(String path) throws IOException {
        String description = FileManager.getFileContentsAsString(path);
        return fromString(description);
    }

    public static CSVTableDescription fromString(String jsonString) throws IOException {
        return (CSVTableDescription) Jsonizer.fromJSON(jsonString, CSVTableDescription.class);
    }

    @JsonProperty("ranges")
    private List<CSVRangeDescription> rangeDescriptionList;

    public List<CSVRangeDescription> getRangeDescriptionList() {
        return rangeDescriptionList;
    }

    public void setRangeDescriptionList(List<CSVRangeDescription> rangeDescriptionList) {
        this.rangeDescriptionList = rangeDescriptionList;
    }

    private DatasetMetadata datasetMetadata;

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CSVTableDescription other = (CSVTableDescription) obj;
        return other.getRangeDescriptionList().equals(this.getRangeDescriptionList());
    }
}
