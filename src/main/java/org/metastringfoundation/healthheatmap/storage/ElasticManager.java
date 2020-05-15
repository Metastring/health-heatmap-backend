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
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.metastringfoundation.data.Dataset;

import java.io.IOException;

@ElasticDatasetStore
public class ElasticManager implements DatasetStore {
    public static final RestHighLevelClient elastic = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")
    ));

    public static void close() throws IOException {
        elastic.close();
    }

    @Override
    public void save(Dataset dataset) {
        System.out.println("Dataset to be saved, but not yet implemented");
    }
}
