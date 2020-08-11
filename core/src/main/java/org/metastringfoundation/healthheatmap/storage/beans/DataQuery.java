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

package org.metastringfoundation.healthheatmap.storage.beans;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataQuery {
    private @Nullable
    Map<String, List<String>> terms;
    private @Nullable
    Map<String, Map<String, String>> ranges;

    public static DataQuery of(@Nullable Map<String, List<String>> terms, @Nullable Map<String, Map<String, String>> ranges) {
        DataQuery dataQuery = new DataQuery();
        dataQuery.setTerms(terms);
        dataQuery.setRanges(ranges);
        return dataQuery;
    }

    public @Nullable
    Map<String, List<String>> getTerms() {
        return terms;
    }

    public void setTerms(@Nullable Map<String, List<String>> terms) {
        this.terms = terms;
    }

    public @Nullable
    Map<String, Map<String, String>> getRanges() {
        return ranges;
    }

    public void setRanges(@Nullable Map<String, Map<String, String>> ranges) {
        this.ranges = ranges;
    }

    @Override
    public String toString() {
        return "DataQuery{" +
                "terms=" + terms +
                ", ranges=" + ranges +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataQuery dataQuery = (DataQuery) o;
        return Objects.equals(terms, dataQuery.terms) &&
                Objects.equals(ranges, dataQuery.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terms, ranges);
    }
}
