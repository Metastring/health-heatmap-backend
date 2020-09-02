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

import org.jboss.logging.Logger;
import org.metastringfoundation.healthheatmap.beanconverters.DataQueryResultToDataResponse;
import org.metastringfoundation.healthheatmap.beanconverters.FilterToDataQuery;
import org.metastringfoundation.healthheatmap.beans.DataResponse;
import org.metastringfoundation.healthheatmap.beans.Filter;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;
import org.metastringfoundation.healthheatmap.helpers.ListAndMapUtils;
import org.metastringfoundation.healthheatmap.web.utils.AppInteraction;
import org.metastringfoundation.healthheatmap.web.utils.CSVDownload;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Path("data")
public class DataResource {
    private static final Logger LOG = Logger.getLogger(DataResource.class);

    private final AppInteraction app;

    @Inject
    public DataResource(AppInteraction app) {
        this.app = app;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse getData(
            Filter filter
    ) throws IOException {
        LOG.debug(filter);
        var result = app.query(FilterToDataQuery.convert(filter));
        return DataQueryResultToDataResponse.convert(result);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("transpose")
    public List<Map<String, String>> transposeData(@QueryParam("dimension") String dimension, FilterAndSelectFields filtersAndFields) throws IOException {
        List<Map<String, String>> filtered = app.getDataForDownload(filtersAndFields);
        return ListAndMapUtils.reshapeCast(filtered, dimension);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/csv")
    @Path("transpose/download")
    public Response downloadTransposedData(@QueryParam("dimension") String dimension, FilterAndSelectFields filtersAndFields) throws IOException {
        List<Map<String, String>> filtered = app.getDataForDownload(filtersAndFields);
        List<Map<String, String>> input = ListAndMapUtils.reshapeCast(filtered, dimension);
        return CSVDownload.getDownloadCSVResponse(input);
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/csv")
    @Path("transpose3d/download")
    public Response downloadData3D(@QueryParam("dimension") String dimension, @QueryParam("3d") List<String> thirdDimension, FilterAndSelectFields filtersAndFields) throws IOException {
        List<Map<String, String>> filtered = app.getDataForDownload(filtersAndFields);
        List<Map<String, String>> input = ListAndMapUtils.reshapeCast(filtered, dimension, thirdDimension);
        return CSVDownload.getDownloadCSVResponse(input);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("transpose3d")
    public List<Map<String, String>> getData3D(@QueryParam("dimension") String dimension, @QueryParam("3d") List<String> thirdDimension, FilterAndSelectFields filtersAndFields) throws IOException {
        List<Map<String, String>> filtered = app.getDataForDownload(filtersAndFields);
        return ListAndMapUtils.reshapeCast(filtered, dimension, thirdDimension);
    }
}
