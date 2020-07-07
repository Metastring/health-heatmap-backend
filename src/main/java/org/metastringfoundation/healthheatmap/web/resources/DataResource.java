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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.beanconverters.DataQueryResultToDataResponse;
import org.metastringfoundation.healthheatmap.logic.beanconverters.DataRequestToDataQuery;
import org.metastringfoundation.healthheatmap.logic.beanconverters.FilterToDataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.web.beans.DataRequest;
import org.metastringfoundation.healthheatmap.web.beans.DataResponse;
import org.metastringfoundation.healthheatmap.web.beans.Filter;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    public DataResponse getData(
            Filter filter
    ) throws IOException {
        LOG.debug(filter);
        DataQueryResult queryResult = app.query(FilterToDataQuery.convertWithoutNormalization(filter));
        return DataQueryResultToDataResponse.convert(queryResult);
    }
}
