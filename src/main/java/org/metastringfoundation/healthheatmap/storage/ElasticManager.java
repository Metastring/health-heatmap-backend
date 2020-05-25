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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.data.Dataset;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;

@ElasticDatasetStore
public class ElasticManager implements DatasetStore {
    public static ObjectMapper objectMapper = new ObjectMapper();
    private final RestHighLevelClient elastic;
    public final String dataIndex = "data";

    public ElasticManager() {
        this("localhost", 9200);
    }

    public ElasticManager(String hostname, int port) {
        elastic = new RestHighLevelClient(RestClient.builder(
                new HttpHost(hostname, port, "http")
        ));
    }

    public void close() throws IOException {
        elastic.close();
    }

    public RestHighLevelClient getElastic() {
        return elastic;
    }

    @Override
    public void save(Dataset dataset) throws IOException {
        BulkRequest request = new BulkRequest();
        for (DataPoint dataPoint : dataset.getData()) {
            request.add(new IndexRequest(dataIndex).source(dataPoint.getAsMap()));
        }
        elastic.bulk(request, RequestOptions.DEFAULT);
    }

    @Override
    public DataQueryResult query(DataQuery dataQuery) throws IOException {
        QueryBuilder query = getElasticQuery(dataQuery);
        SearchRequest searchRequest = getElasticSearchRequest(query);
        SearchResponse searchResponse = elastic.search(searchRequest, RequestOptions.DEFAULT);
        DataQueryResult searchResult = new DataQueryResult();
        searchResult.setResult(searchResponse.toString());
        return searchResult;
    }

    private QueryBuilder getElasticQuery(DataQuery dataQuery) {
        BoolQueryBuilder query = boolQuery();
        for (Map.Entry<String, Collection<String>> mustMatchTerm: dataQuery.getMust().entrySet()) {
            query.must(termsQuery(mustMatchTerm.getKey(), mustMatchTerm.getValue()));
        }
        return query;
    }

    private SearchRequest getElasticSearchRequest(QueryBuilder query) {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(query);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
