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

package org.metastringfoundation.healthheatmap.web.ingestion;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.logic.Application;

import javax.json.Json;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class VerificationResourceTest {
    @Test
    void returns_verification_result() {
        String csvContent = "a,b,c\np,1,2\nq,3,4";
        String rootMetadata = Json.createObjectBuilder()
                .add("fields", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("field", "source")
                                .add("value", "Test Source")))
                .build().toString();
        String specificMetadata = Json.createObjectBuilder()
                .add("fields", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("field", "y")
                                .add("range", "A2:A"))
                        .add(Json.createObjectBuilder()
                                .add("field", "x")
                                .add("range", "B1:1"))
                        .add(Json.createObjectBuilder()
                                .add("field", "value")
                                .add("range", "B2:")))
                .build().toString();

        given()
                .multiPart("file", "source.csv", csvContent.getBytes())
                .multiPart("metadata1", "metadata.json", rootMetadata.getBytes())
                .multiPart("metadata2", "source.metadata.json", specificMetadata.getBytes())
            .when()
                .post("/api/verify")
            .then()
                .statusCode(200)
                .body(
                        "fields.name", hasItems("y", "x", "value", "source")
                );

    }
}