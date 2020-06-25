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

import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.beanconverters.DataQueryResultToDataResponse;
import org.metastringfoundation.healthheatmap.logic.beanconverters.DataRequestToDataQuery;
import org.metastringfoundation.healthheatmap.logic.beanconverters.MultiMapToDataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.web.beans.DataRequest;
import org.metastringfoundation.healthheatmap.web.beans.DataResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;

@Path("data")
public class DataResource {
    private static final Logger LOG = LogManager.getLogger(DataResource.class);

    private final Application app;

    @Inject
    public DataResource(Application app) {
        this.app = app;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse getData(
            @BeanParam DataRequest dataRequest
    ) throws IOException {
        DataQueryResult queryResult = app.query(DataRequestToDataQuery.convert(dataRequest));
        return DataQueryResultToDataResponse.convert(queryResult);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "SWAGGERFAIL: https://github.com/swagger-api/swagger-core/issues/2721\n" +
            "This endpoint actually takes in a MultivaluedMap<String, String> " +
            "(ie, a map with a key which is a string and value which is an array of strings. " +
            "Perhaps you can use curl to send such a JSON as POST body. " +
            "The key should be dimensions like 'indicator', 'source', etc. " +
            "Values should be the value of these dimensions as it appears in the dataset.")
    public DataResponse getData(
            MultivaluedHashMap<String, String> params
    ) throws IOException {
        LOG.debug(params);
        DataQueryResult queryResult = app.query(MultiMapToDataQuery.convert(params));
        return DataQueryResultToDataResponse.convert(queryResult);
    }
}
