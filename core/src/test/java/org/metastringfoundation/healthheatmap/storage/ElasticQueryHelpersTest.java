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

import net.javacrumbs.jsonunit.core.Option;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.beans.Filter;

import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class ElasticQueryHelpersTest {
    @Test
    void getsElasticQueryWhenTermsAndRangesSpecified() {
        Filter filter = Filter.FilterBuilder.aFilter()
                .withTerms(Map.of(
                        "field1", List.of("value1", "value2"),
                        "field2", List.of("value3", "value4")
                ))
                .withRanges(Map.of(
                        "field1", Map.of("gt", "vgt", "lt", "vlt"),
                        "field2", Map.of("lt", "vl2", "gt", "vg2")
                ))
                .build();
        QueryBuilder query = ElasticQueryHelpers.getElasticQuery(filter);
        String actual = query.toString();
        String expected = "{\n" +
                "  \"bool\" : {\n" +
                "    \"filter\" : [\n" +
                "      {\n" +
                "        \"terms\" : {\n" +
                "          \"field2\" : [\n" +
                "            \"value3\",\n" +
                "            \"value4\"\n" +
                "          ],\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"terms\" : {\n" +
                "          \"field1\" : [\n" +
                "            \"value1\",\n" +
                "            \"value2\"\n" +
                "          ],\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"range\" : {\n" +
                "          \"field2\" : {\n" +
                "            \"from\" : \"vg2\",\n" +
                "            \"to\" : \"vl2\",\n" +
                "            \"include_lower\" : false,\n" +
                "            \"include_upper\" : false,\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"range\" : {\n" +
                "          \"field1\" : {\n" +
                "            \"from\" : \"vgt\",\n" +
                "            \"to\" : \"vlt\",\n" +
                "            \"include_lower\" : false,\n" +
                "            \"include_upper\" : false,\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";

        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }

    @Test
    void getsElasticQueryWhenOnlyTermsGiven() {
        Filter filter = Filter.FilterBuilder.aFilter()
                .withTerms(Map.of(
                        "field1", List.of("value1", "value2"),
                        "field2", List.of("value3", "value4")
                ))
                .build();
        QueryBuilder query = ElasticQueryHelpers.getElasticQuery(filter);
        String actual = query.toString();
        String expected = "{\n" +
                "  \"bool\" : {\n" +
                "    \"filter\" : [\n" +
                "      {\n" +
                "        \"terms\" : {\n" +
                "          \"field2\" : [\n" +
                "            \"value3\",\n" +
                "            \"value4\"\n" +
                "          ],\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"terms\" : {\n" +
                "          \"field1\" : [\n" +
                "            \"value1\",\n" +
                "            \"value2\"\n" +
                "          ],\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }

    @Test
    void getsElasticQueryWhenOnlyRangesSpecified() {
        Filter filter = Filter.FilterBuilder.aFilter()
                .withRanges(Map.of(
                        "field1", Map.of("gt", "vgt", "lt", "vlt"),
                        "field2", Map.of("lt", "vl2", "gt", "vg2")
                ))
                .build();
        QueryBuilder query = ElasticQueryHelpers.getElasticQuery(filter);
        String actual = query.toString();
        String expected = "{\n" +
                "  \"bool\" : {\n" +
                "    \"filter\" : [\n" +
                "      {\n" +
                "        \"range\" : {\n" +
                "          \"field2\" : {\n" +
                "            \"from\" : \"vg2\",\n" +
                "            \"to\" : \"vl2\",\n" +
                "            \"include_lower\" : false,\n" +
                "            \"include_upper\" : false,\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"range\" : {\n" +
                "          \"field1\" : {\n" +
                "            \"from\" : \"vgt\",\n" +
                "            \"to\" : \"vlt\",\n" +
                "            \"include_lower\" : false,\n" +
                "            \"include_upper\" : false,\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}";

        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }
}