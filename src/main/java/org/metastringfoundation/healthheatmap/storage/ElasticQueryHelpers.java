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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.web.beans.FilterAndSelectFields;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

public class ElasticQueryHelpers {
    private static final Logger LOG = LogManager.getLogger(ElasticQueryHelpers.class);

    public static @NotNull SearchResponse doSearch(
            @NotNull RestHighLevelClient elastic,
            @NotNull DataQuery dataQuery,
            @NotNull String index
    ) throws IOException {
        QueryBuilder query = getElasticQuery(dataQuery);
        LOG.debug(query.toString());
        SearchRequest searchRequest = getElasticSearchRequest(query, index);
        return elastic.search(searchRequest, RequestOptions.DEFAULT);
    }

    public static @NotNull QueryBuilder getElasticQuery(@NotNull DataQuery dataQuery) {
        BoolQueryBuilder query = boolQuery();
        LOG.debug("Incoming query: " + dataQuery);
        for (Map.Entry<String, Collection<String>> mustMatchTerm : dataQuery.getMust().entrySet()) {
            LOG.debug("Adding must match term: " + mustMatchTerm.getKey());
            query.filter(termsQuery(mustMatchTerm.getKey(), mustMatchTerm.getValue()));
        }
        return query;
    }

    public static @Nullable
    QueryBuilder getElasticQuery(@Nullable MultivaluedHashMap<String, String> queryParams) {
        if (queryParams == null) {
            return null;
        }
        BoolQueryBuilder query = boolQuery();
        for (Map.Entry<String, List<String>> term : queryParams.entrySet()) {
            query.filter(termsQuery(term.getKey(), term.getValue()));
        }
        return query;
    }

    public static @NotNull SearchRequest getElasticSearchRequest(@NotNull QueryBuilder query, @NotNull String index) {
        return getAnySearchRequest(query, null, index, 10000);
    }

    public static @NotNull SearchRequest getAggregationRequest(
            @NotNull AggregationBuilder aggregation,
            @NotNull String index
    ) {
        return getAnySearchRequest(null, aggregation, index, 0);
    }

    public static @NotNull SearchRequest getAnySearchRequest(
            @Nullable QueryBuilder query,
            @Nullable AggregationBuilder aggregation,
            @NotNull String index,
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
