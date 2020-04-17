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
import org.metastringfoundation.healthheatmap.logic.errors.ApplicationError;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("data")
public class Data {
    @Inject
    Application app;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getData(
            @QueryParam("indicator") Long indicator,
            @QueryParam("geography") Long geography,
            @QueryParam("aggregation") String aggregation
    ) {
        try {
            String response = app.getData(indicator, geography, aggregation);
            return Response.ok().entity(response).build();
        } catch (ApplicationError applicationError) {
            return Response.status(503).entity(applicationError.toString()).build();
        }
    }
}
