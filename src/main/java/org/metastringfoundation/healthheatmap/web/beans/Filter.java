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

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.Map;
import java.util.Optional;

public class Filter {
    private MultivaluedHashMap<String, String> terms;
    private Map<String, Map<String, String>> ranges;

    public Optional<MultivaluedHashMap<String, String>> getTerms() {
        return Optional.ofNullable(terms);
    }

    public void setTerms(MultivaluedHashMap<String, String> terms) {
        this.terms = terms;
    }

    public Optional<Map<String, Map<String, String>>> getRanges() {
        return Optional.ofNullable(ranges);
    }

    public void setRanges(Map<String, Map<String, String>> ranges) {
        this.ranges = ranges;
    }
}
