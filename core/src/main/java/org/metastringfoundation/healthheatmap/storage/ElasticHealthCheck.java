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

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

import java.io.IOException;

public class ElasticHealthCheck {
    public static boolean indexes(RestHighLevelClient client, String... indexes) throws IOException {
        ClusterHealthRequest request = new ClusterHealthRequest(indexes);
        ClusterHealthResponse response = client.cluster().health(request, RequestOptions.DEFAULT);
        ClusterHealthStatus status = response.getStatus();
        return status.equals(ClusterHealthStatus.GREEN) || status.equals(ClusterHealthStatus.YELLOW);
    }
}
