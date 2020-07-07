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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.metastringfoundation.healthheatmap.web.beans.FilterAndSelectFields;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.metastringfoundation.healthheatmap.storage.ElasticQueryHelpers.doSearch;
import static org.metastringfoundation.healthheatmap.storage.ElasticQueryHelpers.getAnySearchRequest;

/**
 * Runs a composite aggregation to get all the results of multiple terms queries.
 * The looping with afterKey required is managed within this class so that you don't have to worry about it.
 */
public class ElasticQueryCompositeAggregation {
    private static final String AGGREGATION_NAME = "allTerms";

    private final @Nonnull
    RestHighLevelClient elastic;
    private final @Nonnull
    String index;
    private final @Nonnull
    FilterAndSelectFields filterAndFields;

    private final List<Map<String, Object>> result = new ArrayList<>();

    private @Nullable
    Map<String, Object> afterKey;

    /**
     * The default constructor
     *
     * @param elastic         client through which the aggregation is run
     * @param index           on which the aggregation is run
     * @param filterAndFields filters restrict the scope of the documents and fields are what gets returned in output
     * @throws IOException if elastic is down
     */
    public ElasticQueryCompositeAggregation(
            @Nonnull RestHighLevelClient elastic,
            @Nonnull String index,
            @Nonnull FilterAndSelectFields filterAndFields
    ) throws IOException {
        this.elastic = elastic;
        this.index = index;
        this.filterAndFields = filterAndFields;
        calculateResult();
    }

    private static @Nonnull
    List<Map<String, Object>> termsMapsFrom(@Nonnull Collection<CompositeAggregation.Bucket> buckets) {
        return buckets.stream()
                .map(CompositeAggregation.Bucket::getKey)
                .collect(Collectors.toList());
    }

    private void calculateResult() throws IOException {
        do {
            CompositeAggregationBuilder aggregation = getQueryForTermsOfField();
            QueryBuilder query = getQuery();
            SearchRequest request = getAnySearchRequest(query, aggregation, index, 0);
            SearchResponse response = doSearch(elastic, request);
            CompositeAggregation compositeAggregation = response.getAggregations().get(AGGREGATION_NAME);
            Collection<CompositeAggregation.Bucket> buckets = getCompositeAggregationBuckets(compositeAggregation);
            afterKey = compositeAggregation.afterKey(); // .afterKey() returns null towards the end
            result.addAll(termsMapsFrom(buckets));
        } while (afterKey != null);
    }

    private QueryBuilder getQuery() {
        return filterAndFields.getFilter().map(ElasticQueryHelpers::getElasticQuery).orElse(null);
    }

    private List<CompositeValuesSourceBuilder<?>> getTermsBuilders() {
        return filterAndFields.getFields().stream()
                .map(this::buildTermsSource)
                .collect(Collectors.toList());
    }

    private TermsValuesSourceBuilder buildTermsSource(String fieldName) {
        return new TermsValuesSourceBuilder(fieldName).field(fieldName).order("asc");
    }

    private static Collection<CompositeAggregation.Bucket> getCompositeAggregationBuckets(
            CompositeAggregation compositeAggregation
    ) {
        return compositeAggregation.getBuckets().stream()
                .map(CompositeAggregation.Bucket.class::cast)
                .collect(Collectors.toList());
    }

    private CompositeAggregationBuilder getQueryForTermsOfField() {
        CompositeAggregationBuilder aggregation = AggregationBuilders.composite(
                AGGREGATION_NAME,
                getTermsBuilders()
        );
        if (afterKey != null && !afterKey.isEmpty()) {
            aggregation.aggregateAfter(afterKey);
        }
        return aggregation;
    }

    /**
     * Gives you the result like this
     * [
     * {"key1": "value1", "key2": "value2"},
     * {"key1": "value3", "key2": "value4"}
     * ]
     * <p>
     * which is equivalent to a CSV
     * <p>
     * key1, key2
     * value1, value2
     * value3, value4
     *
     * @return a list of records where each record is key-value pairs
     */
    public List<Map<String, Object>> getResult() {
        return result;
    }
}
