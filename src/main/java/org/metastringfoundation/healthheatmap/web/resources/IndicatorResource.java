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

import org.metastringfoundation.healthheatmap.web.beans.Filter;
import org.metastringfoundation.healthheatmap.web.beans.FilterAndSelectFields;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Path("indicators")
public class IndicatorResource {
    @Context
    ResourceContext resourceContext;

    private static List<Map<String, Object>> getFromDimensions(ResourceContext resourceContext, List<String> fields) throws IOException {
        DimensionsResource dimensions = resourceContext.getResource(DimensionsResource.class);
        FilterAndSelectFields filter = new FilterAndSelectFields();
        filter.setFields(fields);
        return dimensions.exportAnyFieldAdvanced(filter);
    }

    private static List<Map<String, Object>> getFromDimensions(ResourceContext resourceContext, FilterAndSelectFields filter) throws IOException {
        DimensionsResource dimensions = resourceContext.getResource(DimensionsResource.class);
        return dimensions.exportAnyFieldAdvanced(filter);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> getAllIndicators() throws IOException {
        return getFromDimensions(resourceContext, List.of(
                "source",
                "indicator_universal_name",
                "indicator_category",
                "indicator_subcategory",
                "indicator_positive_negative"
        ));
    }

    @GET
    @Path("/{indicator_universal_name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getOneIndicator(@PathParam("indicator_universal_name") String name) throws IOException {
        Filter indicatorFilter = new Filter();
        indicatorFilter.setTerms(Map.of("indicator_universal_name", List.of(name)));
        FilterAndSelectFields filter = new FilterAndSelectFields();
        filter.setFilter(indicatorFilter);
        filter.setFields(List.of(
                "source",
                "indicator_universal_name",
                "indicator_category",
                "indicator_subcategory",
                "indicator_positive_negative",
                "indicator_type",
                "indicator_definition",
                "indicator_methodOfEstimation"
        ));
        List<Map<String, Object>> result = getFromDimensions(resourceContext, filter);
        return resultToSingleMap(result);
    }

    private Map<String, Object> resultToSingleMap(List<Map<String, Object>> input) {
        Set<String> fields = input.stream()
                .flatMap(e -> e.keySet().stream())
                .collect(Collectors.toSet());

        Map<String, Object> result = new HashMap<>();

        Map<String, Map<String, Object>> sourceSpecific = new HashMap<>();

        Set<String> commonFields = new HashSet<>();

        for (String field : fields) {
            Set<String> values = input.stream().map(e -> e.get(field).toString()).collect(Collectors.toSet());
            if (values.size() == 1) {
                result.put(field, values.stream().findFirst().get());
                commonFields.add(field);
            }
        }

        for (Map<String, Object> oneSource : input) {
            commonFields.forEach(oneSource::remove);
            String source = oneSource.get("source").toString();
            oneSource.remove("source");
            sourceSpecific.put(source, oneSource);
        }

        result.put("source_specific", sourceSpecific);

        return result;

    }

}
