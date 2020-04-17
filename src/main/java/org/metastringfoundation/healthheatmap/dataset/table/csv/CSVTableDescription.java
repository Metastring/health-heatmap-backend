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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.metastringfoundation.healthheatmap.dataset.DatasetMetadata;
import org.metastringfoundation.healthheatmap.helpers.FileManager;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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

    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DatasetMetadata datasetMetadata;

    public DatasetMetadata getDatasetMetadata() {
        return datasetMetadata;
    }

    public void setDatasetMetadata(DatasetMetadata datasetMetadata) {
        this.datasetMetadata = datasetMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CSVTableDescription that = (CSVTableDescription) o;
        return Objects.equals(rangeDescriptionList, that.rangeDescriptionList) &&
                Objects.equals(datasetMetadata, that.datasetMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rangeDescriptionList, datasetMetadata);
    }

    @Override
    public String toString() {
        return "CSVTableDescription{" +
                "rangeDescriptionList=" + rangeDescriptionList +
                ", datasetMetadata=" + datasetMetadata +
                '}';
    }
}
