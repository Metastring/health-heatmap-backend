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

package org.metastringfoundation.healthheatmap.web.beans;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class Filter {
    private @Nullable
    Map<String, List<String>> terms;
    private @Nullable
    Map<String, Map<String, String>> ranges;

    @Nullable
    public Map<String, List<String>> getTerms() {
        return terms;
    }

    public void setTerms(@Nullable Map<String, List<String>> terms) {
        this.terms = terms;
    }

    @Nullable
    public Map<String, Map<String, String>> getRanges() {
        return ranges;
    }

    public void setRanges(@Nullable Map<String, Map<String, String>> ranges) {
        this.ranges = ranges;
    }

    public static final class FilterBuilder {
        private
        Map<String, List<String>> terms;
        private
        Map<String, Map<String, String>> ranges;

        private FilterBuilder() {
        }

        public static FilterBuilder aFilter() {
            return new FilterBuilder();
        }

        public FilterBuilder withTerms(Map<String, List<String>> terms) {
            this.terms = terms;
            return this;
        }

        public FilterBuilder withRanges(Map<String, Map<String, String>> ranges) {
            this.ranges = ranges;
            return this;
        }

        public FilterBuilder but() {
            return aFilter().withTerms(terms).withRanges(ranges);
        }

        public Filter build() {
            Filter filter = new Filter();
            filter.setTerms(terms);
            filter.setRanges(ranges);
            return filter;
        }
    }
}
