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

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;
import org.metastringfoundation.datareader.dataset.map.MapDataset;
import org.metastringfoundation.healthheatmap.helpers.HealthDatasetFromDataset;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class ElasticTest {
    @Container
    private static final ElasticsearchContainer ELASTICSEARCH_CONTAINER;
    static {
        ELASTICSEARCH_CONTAINER = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:7.7.0");
        ELASTICSEARCH_CONTAINER.start();
    }
    private static final ElasticManager elasticManager = new ElasticManager(ELASTICSEARCH_CONTAINER.getHost(), ELASTICSEARCH_CONTAINER.getFirstMappedPort());

    private void refreshIndex() throws IOException {
        elasticManager.getElastic().indices().refresh(new RefreshRequest(elasticManager.dataIndex), RequestOptions.DEFAULT);
    }

    @Test
    public void querySavedData() throws IOException {
        List<Map<String, String>> data = List.of(
                Map.of("indicator", "mmr", "entity.district", "kannur", "value", "1"),
                Map.of("indicator", "mmr", "entity.district", "kozhikkode", "value", "1.2"),
                Map.of("indicator", "u5mr", "entity.district", "kozhikkode", "value", "1")
        );
        elasticManager.save(new HealthDatasetFromDataset(new MapDataset(data)));
        DataQuery dataQuery = new DataQuery();
        dataQuery.setMust(Map.of("indicator", List.of("mmr")));
        refreshIndex();
        DataQueryResult actual = elasticManager.query(dataQuery);
        assertEquals(List.of(
                Map.of("indicator", "mmr", "entity.district", "kozhikkode", "value", "1.2"),
                Map.of("indicator", "mmr", "entity.district", "kannur", "value", "1")
            ), actual.getResult());
    }
}
