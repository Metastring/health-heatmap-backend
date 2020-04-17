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
import org.metastringfoundation.healthheatmap.dataset.entities.UnmatchedReport;
import org.metastringfoundation.healthheatmap.dataset.entities.UnmatchedSource;
import org.metastringfoundation.healthheatmap.entities.TimePeriod;

import java.util.Objects;

public class DatasetMetadata {
    private UnmatchedSource source;
    private UnmatchedReport report;

    @JsonProperty("duration")
    private TimePeriod timePeriod;

    public DatasetMetadata() {}

    public DatasetMetadata(@JsonProperty("source") UnmatchedSource source,
                           @JsonProperty("report") UnmatchedReport report) {
        this.source = source;
        this.report = report;
    }

    public UnmatchedSource getSource() {
        return source;
    }

    public void setSource(UnmatchedSource source) {
        this.source = source;
    }

    public UnmatchedReport getReport() {
        return report;
    }

    public void setReport(UnmatchedReport report) {
        this.report = report;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetMetadata that = (DatasetMetadata) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(report, that.report);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, report);
    }

    @Override
    public String toString() {
        return "DatasetMetadata{" +
                "source=" + source +
                ", report=" + report +
                '}';
    }
}
