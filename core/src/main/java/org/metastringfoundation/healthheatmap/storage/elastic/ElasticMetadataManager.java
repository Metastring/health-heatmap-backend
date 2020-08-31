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

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;
import org.metastringfoundation.healthheatmap.beans.DownloadRequest;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;
import org.metastringfoundation.healthheatmap.logic.ApplicationMetadataStore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ElasticStore
@ApplicationScoped
public class ElasticMetadataManager implements ApplicationMetadataStore {
    private static final Logger LOG = Logger.getLogger(ElasticMetadataManager.class);
    public static final String SAVED_DATA_FILE = "savedDataFile";
    private final RestHighLevelClient elastic;
    private final String downloadsIndex = "downloads";
    private final String metadataIndex = "metadata";

    @Inject
    public ElasticMetadataManager(RestHighLevelClient elastic) {
        this.elastic = elastic;
    }


    @Override
    public void factoryReset() throws IOException {
        deleteIndex();
        recreateIndex();
    }

    private void recreateIndex() throws IOException {
        CreateIndexRequest create = new CreateIndexRequest(metadataIndex);
        elastic.indices().create(create, RequestOptions.DEFAULT);
    }

    private void deleteIndex() throws IOException {
        LOG.info("Deleting index: " + metadataIndex);
        DeleteIndexRequest request = new DeleteIndexRequest(metadataIndex);
        try {
            elastic.indices().delete(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchException ex) {
            LOG.info("Tried deleting a non-existent index. Oops!");
        }
    }

    @Override
    public void logDownload(DownloadRequest downloadRequest) throws IOException {
        IndexRequest request = new IndexRequest(downloadsIndex);
        String jsonData = Jsonizer.asJSON(downloadRequest);
        request.source(jsonData, XContentType.JSON);
        elastic.index(request, RequestOptions.DEFAULT);
    }

    @Override
    public void markDatafileAsSaved(String datafile) throws IOException {
        IndexRequest request = new IndexRequest(metadataIndex);
        String jsonData = Jsonizer.asJSON(Map.of("file", datafile, "type", SAVED_DATA_FILE));
        request.source(jsonData, XContentType.JSON);
        elastic.index(request, RequestOptions.DEFAULT);
    }

    @Override
    public boolean getHealth() throws IOException {
        return ElasticHealthCheck.indexes(elastic, downloadsIndex, metadataIndex);
    }

    @Override
    public List<String> getSavedDataFiles() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(1000);
        searchRequest.indices(metadataIndex);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = elastic.search(searchRequest, RequestOptions.DEFAULT);
        return Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .filter(m -> m.get("type").equals(SAVED_DATA_FILE))
                .map(m -> m.get("file"))
                .map(e -> (String) e)
                .collect(Collectors.toList());
    }
}
