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

package org.metastringfoundation.healthheatmap.storage;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.metastringfoundation.healthheatmap.storage.ElasticQueryHelpers.doSearch;
import static org.metastringfoundation.healthheatmap.storage.ElasticQueryHelpers.getAggregationRequest;

public class ElasticQueryCompositeAggregation {
    private static final String AGGREGATION_NAME = "allTerms";

    private final RestHighLevelClient elastic;
    private final String index;
    private final List<String> fields;

    private final List<Map<String, Object>> result = new ArrayList<>();

    private Map<String, Object> afterKey = new HashMap<>();

    public ElasticQueryCompositeAggregation(RestHighLevelClient elastic, String index, List<String> fields) throws IOException {
        this.elastic = elastic;
        this.index = index;
        this.fields = fields;
        calculateResult();
    }

    private static Collection<CompositeAggregation.Bucket> getCompositeAggregationBuckets(
            CompositeAggregation compositeAggregation
    ) {
        return compositeAggregation.getBuckets().stream()
                .map(CompositeAggregation.Bucket.class::cast)
                .collect(Collectors.toList());
    }

    private static @NotNull List<Map<String, Object>> termsMapsFrom(@NotNull Collection<CompositeAggregation.Bucket> buckets) {
        return buckets.stream()
                .map(CompositeAggregation.Bucket::getKey)
                .collect(Collectors.toList());
    }

    private void calculateResult() throws IOException {
        do {
            CompositeAggregationBuilder aggregation = getQueryForTermsOfField();
            SearchRequest request = getAggregationRequest(aggregation, index);
            SearchResponse response = doSearch(elastic, request);
            CompositeAggregation compositeAggregation = response.getAggregations().get(AGGREGATION_NAME);
            Collection<CompositeAggregation.Bucket> buckets = getCompositeAggregationBuckets(compositeAggregation);
            afterKey = compositeAggregation.afterKey();
            result.addAll(termsMapsFrom(buckets));
        } while (afterKey != null);
    }

    private CompositeAggregationBuilder getQueryForTermsOfField() {
        CompositeAggregationBuilder aggregation = AggregationBuilders.composite(
                AGGREGATION_NAME,
                getTermsBuilders()
        );
        if (!afterKey.isEmpty()) {
            aggregation.aggregateAfter(afterKey);
        }
        return aggregation;
    }

    private List<CompositeValuesSourceBuilder<?>> getTermsBuilders() {
        return fields.stream()
                .map(this::buildTermsSource)
                .collect(Collectors.toList());
    }

    private TermsValuesSourceBuilder buildTermsSource(String fieldName) {
        return new TermsValuesSourceBuilder(fieldName).field(fieldName).order("asc");
    }

    public List<Map<String, Object>> getResult() {
        return result;
    }
}
