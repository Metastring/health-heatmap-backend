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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.metastringfoundation.data.DataPoint;
import org.metastringfoundation.data.Dataset;

import java.io.IOException;

@ElasticDatasetStore
public class ElasticManager implements DatasetStore {
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static final RestHighLevelClient elastic = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")
    ));

    public static void close() throws IOException {
        elastic.close();
    }

    @Override
    public void save(Dataset dataset) throws IOException {
        BulkRequest request = new BulkRequest();
        for (DataPoint dataPoint: dataset.getData()) {
            request.add(new IndexRequest("data").source(dataPoint.getAsMap()));
        }
        elastic.bulk(request, RequestOptions.DEFAULT);
    }
}
