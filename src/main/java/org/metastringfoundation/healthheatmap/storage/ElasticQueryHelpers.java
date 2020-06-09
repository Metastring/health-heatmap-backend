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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

public class ElasticQueryHelpers {

    public static @NotNull SearchResponse doSearch(
            @NotNull RestHighLevelClient elastic,
            @NotNull DataQuery dataQuery,
            @NotNull String index
    ) throws IOException {
        QueryBuilder query = getElasticQuery(dataQuery);
        SearchRequest searchRequest = getElasticSearchRequest(query, index);
        return elastic.search(searchRequest, RequestOptions.DEFAULT);
    }

    public static @NotNull QueryBuilder getElasticQuery(@NotNull DataQuery dataQuery) {
        BoolQueryBuilder query = boolQuery();
        for (Map.Entry<String, Collection<String>> mustMatchTerm : dataQuery.getMust().entrySet()) {
            query.must(termsQuery(mustMatchTerm.getKey(), mustMatchTerm.getValue()));
        }
        return query;
    }

    public static @NotNull SearchRequest getElasticSearchRequest(@NotNull QueryBuilder query, @NotNull String index) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(query);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    public static Map<String, String> convertToStringOnlyMap(Map<String, Object> input) {
        return input.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }
}
