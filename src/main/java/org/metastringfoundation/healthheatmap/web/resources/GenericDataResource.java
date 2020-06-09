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

import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.beanconverters.DataQueryResultToDataResponse;
import org.metastringfoundation.healthheatmap.logic.beanconverters.DataRequestToDataQuery;
import org.metastringfoundation.healthheatmap.logic.beanconverters.MultiMapToDataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.web.beans.DataResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("multidata")
public class GenericDataResource {
    private final Application app;

    @Inject
    public GenericDataResource(Application app) {
        this.app = app;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DataResponse getData(
            @Context UriInfo uriInfo
            ) throws IOException {
        MultivaluedMap<String, String> params = uriInfo.getPathParameters();
        DataQueryResult queryResult = app.query(MultiMapToDataQuery.convert(params));
        return DataQueryResultToDataResponse.convert(queryResult);
    }
}
