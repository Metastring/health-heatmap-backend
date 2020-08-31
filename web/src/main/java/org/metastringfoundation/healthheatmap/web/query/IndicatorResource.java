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

package org.metastringfoundation.healthheatmap.web.query;

import org.metastringfoundation.healthheatmap.beans.Filter;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                "source.id",
                "indicator.id",
                "indicator.Name",
                "indicator.Category",
                "indicator.Sub-Category",
                "indicator.Positive/Negative"
        ));
    }

    @GET
    @Path("/{indicator.id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getOneIndicator(@PathParam("indicator.id") String name) throws IOException {
        Filter indicatorFilter = new Filter();
        indicatorFilter.setTerms(Map.of("indicator.id", List.of(name)));
        FilterAndSelectFields filter = new FilterAndSelectFields();
        filter.setFilter(indicatorFilter);
        filter.setFields(List.of(
                "source.id",
                "indicator.id",
                "indicator.Name",
                "indicator.Category",
                "indicator.Sub-Category",
                "indicator.Positive/Negative",
                "indicator.Definition",
                "upstream.License",
                "upstream.License URL"
        ));
        List<Map<String, Object>> result = getFromDimensions(resourceContext, filter);
        return resultToSingleMap(result);
    }

    private Map<String, Object> resultToSingleMap(List<Map<String, Object>> input) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Object>> sourceSpecific = new HashMap<>();

        Set<String> commonFields = Set.of(
                "indicator.id",
                "indicator.Name",
                "indicator.Category",
                "indicator.Sub-Category",
                "indicator.Positive/Negative",
                "indicator.Definition"
        );

        Set<String> sourceSpecificFields = Set.of(
                "upstream.License",
                "upstream.License URL"
        );

        input.forEach(indicatorData -> {
            Map<String, Object> thisSourceSpecific = new HashMap<>();
            commonFields.forEach(field -> result.put(field, indicatorData.get(field)));
            sourceSpecificFields.forEach(field -> thisSourceSpecific.put(field, indicatorData.get(field)));
            String source = (String) indicatorData.get("source.id");
            sourceSpecific.put(source, thisSourceSpecific);
        });
        result.put("source_specific", sourceSpecific);
        return result;
    }

}
