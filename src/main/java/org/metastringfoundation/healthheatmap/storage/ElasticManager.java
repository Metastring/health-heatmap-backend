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

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.metastringfoundation.healthheatmap.storage.ElasticQueryHelpers.doSearch;

@ElasticStore
public class ElasticManager implements DatasetStore {
    private static final Logger LOG = LogManager.getLogger(ElasticManager.class);
    private final RestHighLevelClient elastic;
    public final String dataIndex = "data";

    public ElasticManager() {
        this("localhost", 9200);
    }

    public ElasticManager(String hostname, int port) {
        LOG.debug("Creating new elasticmanager instance");
        elastic = new RestHighLevelClient(RestClient.builder(
                new HttpHost(hostname, port, "http")
        ));
    }

    public void shutdown() throws IOException {
        elastic.close();
    }

    public RestHighLevelClient getElastic() {
        return elastic;
    }

    @Override
    public void save(HealthDataset dataset) throws IOException {
        BulkRequest request = new BulkRequest();
        for (Map<String, String> dataPoint : dataset.getData()) {
            request.add(new IndexRequest(dataIndex).source(dataPoint));
        }
        elastic.bulk(request, RequestOptions.DEFAULT);
    }

    @Override
    public void factoryReset() throws IOException {
        deleteIndex();
        createIndexWithCorrectDynamicMapping();
    }

    private void createIndexWithCorrectDynamicMapping() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(dataIndex);
        XContentBuilder dynamicTemplate = XContentFactory.jsonBuilder();
        dynamicTemplate.startObject();
        dynamicTemplate.startArray("dynamic_templates");
        {
            dynamicTemplate.startObject();
            {
                dynamicTemplate.startObject("strings_as_keywords");
                {
                    dynamicTemplate.field("match_mapping_type", "string");
                    dynamicTemplate.startObject("mapping");
                    {
                        dynamicTemplate.field("type", "keyword");
                    }
                    dynamicTemplate.endObject();
                }
                dynamicTemplate.endObject();
            }
            dynamicTemplate.endObject();
        }
        dynamicTemplate.endArray();
        dynamicTemplate.endObject();
        request.mapping(dynamicTemplate);
        elastic.indices().create(request, RequestOptions.DEFAULT);
    }

    private void deleteIndex() throws IOException {
        LOG.info("Deleting index: " + dataIndex);
        DeleteIndexRequest request = new DeleteIndexRequest(dataIndex);
        try {
            elastic.indices().delete(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchException ex) {
            LOG.info("Ignoring missing index");
        }
    }

    @Override
    public DataQueryResult query(DataQuery dataQuery) throws IOException {
        DataQueryResult searchResult = new DataQueryResult();
        SearchResponse searchResponse = doSearch(elastic, dataQuery, dataIndex);
        LOG.debug(searchResponse.toString());
        searchResult.setResult(Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .map(ElasticQueryHelpers::convertToStringOnlyMap)
                .collect(Collectors.toList())
        );
        return searchResult;
    }

    @Override
    public List<Map<String, Object>> getAllTermsOfFields(List<String> fields) throws IOException {
        return ElasticQueryHelpers.getAllTermsOfFields(elastic, dataIndex, fields);
    }
}
