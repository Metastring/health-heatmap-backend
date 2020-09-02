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

import com.google.common.collect.Lists;
import org.apache.http.HttpHost;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
import org.jboss.logging.Logger;
import org.metastringfoundation.healthheatmap.beans.Filter;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.logic.DatasetStore;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.metastringfoundation.healthheatmap.storage.elastic.ElasticQueryHelpers.doSearch;

@ElasticStore
@ApplicationScoped
public class ElasticManager implements DatasetStore {
    private static final Logger LOG = Logger.getLogger(ElasticManager.class);
    private final RestHighLevelClient elastic;
    private final String dataIndex;

    public ElasticManager(String hostname, int port, String dataIndex) {
        LOG.debug("Creating new elasticmanager instance");
        elastic = new RestHighLevelClient(RestClient.builder(
                new HttpHost(hostname, port, "http")
        ));
        this.dataIndex = dataIndex;
    }

    @Inject
    public ElasticManager(RestHighLevelClient elastic, @ConfigProperty(name = "hhm.elastic.data.index", defaultValue = "data") String dataIndex) {
        this.elastic = elastic;
        this.dataIndex = dataIndex;
    }

    public void shutdown() throws IOException {
        elastic.close();
    }

    public RestHighLevelClient getElastic() {
        return elastic;
    }

    public String getDataIndexName() {
        return dataIndex;
    }

    @Override
    public void save(HealthDataset dataset) throws IOException {
        for (List<? extends Map<String, String>> dataPointsBatch : Lists.partition(dataset.getData(), 5000)) {
            BulkRequest request = new BulkRequest();
            dataPointsBatch.forEach(dataPoint -> request.add(new IndexRequest(dataIndex).source(dataPoint)));
            elastic.bulk(request, RequestOptions.DEFAULT);
        }
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
            LOG.info("Tried deleting a non-existent index. Oops!");
        }
    }

    @Override
    public DataQueryResult query(DataQuery dataQuery) throws IOException {
        DataQueryResult searchResult = new DataQueryResult();
        SearchResponse searchResponse = doSearch(elastic, dataQuery, dataIndex);
        LOG.debug(searchResponse.toString());
        searchResult.setResult(Arrays.stream(searchResponse.getHits().getHits())
                .map(this::getHitAsMapWithId)
                .map(ElasticQueryHelpers::convertToStringOnlyMap)
                .collect(Collectors.toList())
        );
        return searchResult;
    }

    private Map<String, Object> getHitAsMapWithId(SearchHit hit) {
        Map<String, Object> mapWithId = hit.getSourceAsMap();
        mapWithId.put("_id", hit.getId());
        return mapWithId;
    }

    @Override
    public List<Map<String, Object>> getAllTermsOfFields(FilterAndSelectFields filterAndFields) throws IOException {
        return ElasticQueryHelpers.getAllTermsOfFields(elastic, dataIndex, filterAndFields);
    }


    @Override
    public boolean getHealth() throws IOException {
        return ElasticHealthCheck.indexes(elastic, dataIndex);
    }

    @Override
    public Map<String, List<String>> getDimensionsPossibleAt(List<String> dimensions, Filter filter) {
        return dimensions.stream()
                .map(d -> getDimensionsPossibleAt(d, filter))
                .flatMap(Collection::stream)
                .flatMap(e -> e.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(e -> (String) e.getValue(), Collectors.toList())));
    }

    private List<Map<String, Object>> getDimensionsPossibleAt(String dimension, Filter filter) {
        FilterAndSelectFields filterAndSelectFields = new FilterAndSelectFields();
        filterAndSelectFields.setFilter(filter);
        filterAndSelectFields.setFields(List.of(dimension));
        try {
            return ElasticQueryHelpers.getAllTermsOfFields(elastic, dataIndex, filterAndSelectFields);
        } catch (IOException e) {
            return List.of();
        }
    }
}
