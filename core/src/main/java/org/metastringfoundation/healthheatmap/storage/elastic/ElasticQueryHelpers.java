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

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;
import org.metastringfoundation.healthheatmap.beanconverters.FilterToDataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.beans.Filter;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collection of various utility functions to interact with elastic search
 */
public class ElasticQueryHelpers {
    private static final Logger LOG = Logger.getLogger(ElasticQueryHelpers.class);

    /**
     * Queries elastic for data using filters supplied
     *
     * @param elastic   the client
     * @param dataQuery the query (including filters)
     * @param index     the index in which to search
     * @return data that matches the filters
     * @throws IOException for connection issues
     */
    public static SearchResponse doSearch(
            @Nonnull RestHighLevelClient elastic,
            @Nonnull DataQuery dataQuery,
            @Nonnull String index
    ) throws IOException {
        QueryBuilder query = getElasticQuery(dataQuery);
        LOG.debug(query.toString());
        SearchRequest searchRequest = getElasticSearchRequest(query, index);
        return elastic.search(searchRequest, RequestOptions.DEFAULT);
    }

    /**
     * simplifies going from filter to elastic query by doing conversion within
     *
     * @param filter the filter
     * @return the query
     * @see #getElasticQuery(DataQuery)
     */
    public static @Nonnull
    QueryBuilder getElasticQuery(@Nonnull Filter filter) {
        return getElasticQuery(FilterToDataQuery.convert(filter));
    }

    /**
     * Converts a generic data query to elastic specific query
     *
     * @param dataQuery terms, ranges, etc can be filtered by
     * @return the corresponding elastic filter query
     */
    public static @Nonnull
    QueryBuilder getElasticQuery(@Nonnull DataQuery dataQuery) {
        LOG.debug("Incoming query: " + dataQuery);
        QueryBuilder query = ElasticFilterQuery.getQuery(dataQuery);
        LOG.debug("Query generated: \n" + query.toString());
        return query;
    }

    public static @Nonnull
    SearchRequest getElasticSearchRequest(@Nonnull QueryBuilder query, @Nonnull String index) {
        return getAnySearchRequest(query, null, index, 10000);
    }

    public static @Nonnull
    SearchRequest getAggregationRequest(
            @Nonnull AggregationBuilder aggregation,
            @Nonnull String index
    ) {
        return getAnySearchRequest(null, aggregation, index, 0);
    }

    public static @Nonnull
    SearchRequest getAnySearchRequest(
            @Nullable QueryBuilder query,
            @Nullable AggregationBuilder aggregation,
            @Nonnull String index,
            int size
    ) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(size);
        if (query != null) {
            searchSourceBuilder.query(query);
        }
        if (aggregation != null) {
            searchSourceBuilder.aggregation(aggregation);
        }
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    public static Map<String, String> convertToStringOnlyMap(Map<String, Object> input) {
        return input.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }

    public static List<Map<String, Object>> getAllTermsOfFields(RestHighLevelClient elastic, String index, FilterAndSelectFields filterAndFields) throws IOException {
        ElasticQueryCompositeAggregation query = new ElasticQueryCompositeAggregation(
                elastic,
                index,
                filterAndFields
        );
        return query.getResult();
    }


    public static SearchResponse doSearch(
            RestHighLevelClient elastic,
            SearchRequest searchRequest
    ) throws IOException {
        return elastic.search(searchRequest, RequestOptions.DEFAULT);
    }
}
