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

package org.metastringfoundation.healthheatmap.web.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;

@QuarkusTest
class DataResourceTest {

    @InjectMock
    Application mockApp;

    @Test
    void getData() throws IOException {
        Mockito.when(mockApp.query(DataQuery.of(Map.of("t1", List.of("tk1", "tk2")), null)))
                .thenReturn(DataQueryResult.of(List.of(Map.of("t1", "tk1", "value", "12"))));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("terms", Map.of("t1", List.of("tk1", "tk2"))))
        .when()
                .post("/api/data")
        .then()
                .statusCode(200)
                .body(
                        "data.t1", hasItems("tk1"),
                        "data.value", hasItems("12")
                );
    }
}