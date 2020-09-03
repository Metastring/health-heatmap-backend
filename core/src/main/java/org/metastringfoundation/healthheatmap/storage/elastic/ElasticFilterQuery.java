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

package org.metastringfoundation.healthheatmap.storage.elastic;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

public class ElasticFilterQuery {
    private final BoolQueryBuilder query = boolQuery();

    private final @Nullable
    Map<String, List<String>> terms;

    private final @Nullable
    Map<String, Map<String, String>> ranges;

    private ElasticFilterQuery(@Nullable Map<String, List<String>> terms, @Nullable Map<String, Map<String, String>> ranges) {
        this.terms = terms;
        this.ranges = ranges;
        calculateQuery();
    }

    public static QueryBuilder getQuery(DataQuery dataQuery) {
        ElasticFilterQuery filterQuery = new ElasticFilterQuery(dataQuery.getTerms(), dataQuery.getRanges());
        return filterQuery.getQuery();
    }

    private void calculateQuery() {
        addTermsQuery();
        addRangesQuery();
    }

    private void addTermsQuery() {
        // We could have done a simple terms: ["term1", "term2"] if all we had to do was that.
        // But, we also want to enable the end user to specify a special term called "null"
        // When null is passed as a term we want to match data that doesn't have that field at all
        // And this can be accomplished only through a "must_not: exists: field" query
        // Which means we now have a condition like field: terms: ["some", "term"] OR must_not: exists: field
        if (terms != null) {
            for (Map.Entry<String, List<String>> mustMatchTerm : terms.entrySet()) {
                String field = mustMatchTerm.getKey();
                if (mustMatchTerm.getValue().contains(null)) {
                    List<String> nonNullTerms = mustMatchTerm.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList());
                    query.filter(boolQuery().should(termsQuery(field, nonNullTerms)).should(boolQuery().mustNot(existsQuery(field))).minimumShouldMatch(1));
                } else {
                    query.filter(termsQuery(mustMatchTerm.getKey(), mustMatchTerm.getValue()));
                }
            }
        }
    }

    private void addRangesQuery() {
        if (ranges != null) {
            for (Map.Entry<String, Map<String, String>> range : ranges.entrySet()) {
                query.filter(getRangeQuery(range));
            }
        }
    }

    private QueryBuilder getRangeQuery(Map.Entry<String, Map<String, String>> range) {
        RangeQueryBuilder query = rangeQuery(range.getKey());
        Map<String, String> filters = range.getValue();
        if (filters.containsKey("lt")) query.lt(filters.get("lt"));
        if (filters.containsKey("lte")) query.lte(filters.get("lte"));
        if (filters.containsKey("gte")) query.gte(filters.get("gte"));
        if (filters.containsKey("gt")) query.gt(filters.get("gt"));
        return query;
    }

    private BoolQueryBuilder getQuery() {
        return query;
    }
}
