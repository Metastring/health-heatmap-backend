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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.etl.KeyValuePairsToCSV;
import org.metastringfoundation.healthheatmap.web.utils.CSVDownload;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Path("dimensions")
public class DimensionsResource {
    private static final Logger LOG = Logger.getLogger(DimensionsResource.class);
    private final Application app;

    @Inject
    public DimensionsResource(Application app) {
        this.app = app;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Fetch all available combinations of specified dimensions",
            description = "If a single dimension is passed, all the values of that dimension is returned. " +
                    "If multiple dimensions are given, a cross product of these are returned " +
                    "with only the combinations that are available in the datasets"
    )
    public String exportAnyFieldsAsCSV(@QueryParam("include") List<String> fields) throws IOException {
        List<Map<String, Object>> fieldCombos = getFieldsWithoutAnyFilter(fields);
        return KeyValuePairsToCSV.convertToCSVWithFirstElementKeysAsHeaders(fieldCombos);
    }

    @GET
    @Produces("text/csv")
    @Path("download")
    @Operation(
            summary = "Triggers download as CSV",
            description = "Same as the parent, but triggers a CSV download"
    )
    public Response exportAnyFieldAsCSVTriggerDownload(@QueryParam("include") List<String> fields) throws IOException {
        String result = exportAnyFieldsAsCSV(fields);
        return CSVDownload.getCSVDownloadTriggeringResponse(result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> exportAnyField(@QueryParam("include") List<String> fields) throws IOException {
        LOG.debug("Fetching " + fields);
        List<Map<String, Object>> result = getFieldsWithoutAnyFilter(fields);
        LOG.debug(result);
        return result;
    }

    private List<Map<String, Object>> getFieldsWithoutAnyFilter(List<String> fields) throws IOException {
        FilterAndSelectFields filterAndFields = new FilterAndSelectFields();
        filterAndFields.setFields(fields);
        return app.getAllTermsOfFields(filterAndFields);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Similar to the GET version, but allows restricting the dataset universe through a filter",
            description = "The returned combination will be restricted to the datapoints that pass the filter given"
    )
    public List<Map<String, Object>> exportAnyFieldAdvanced(FilterAndSelectFields filterAndFields) throws IOException {
        List<Map<String, Object>> result = getFields(filterAndFields);
        LOG.debug(result);
        return result;
    }

    @POST
    @Path("download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/csv")
    @Operation(
            summary = "Same as the parent, but triggers CSV download"
    )
    public Response exportAnyFieldAdvancedTriggerDownload(FilterAndSelectFields filterAndFields) throws IOException {
        List<Map<String, Object>> result = getFields(filterAndFields);
        LOG.debug(result);
        String resultString = KeyValuePairsToCSV.convertToCSVWithFirstElementKeysAsHeaders(result);
        return CSVDownload.getCSVDownloadTriggeringResponse(resultString);
    }

    private List<Map<String, Object>> getFields(FilterAndSelectFields filterAndFields) throws IOException {
        return app.getAllTermsOfFields(filterAndFields);
    }

}
