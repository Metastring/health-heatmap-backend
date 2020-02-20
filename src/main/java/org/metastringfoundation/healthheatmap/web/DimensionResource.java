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

package org.metastringfoundation.healthheatmap.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.ApplicationError;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("{dimension: indicator|entity}")
public class DimensionResource {
    private static final Logger LOG = LogManager.getLogger(DimensionResource.class);

    @Inject
    Application app;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDimension(
            @PathParam("dimension") String dimension
    ) {
        try {
            switch (dimension) {
                case "indicator":
                    return Response.status(200).entity(app.getIndicators()).build();
                case "entity":
                    return Response.status(200).entity(app.getEntities()).build();
                default:
                    return Response.status(200).entity(dimension).build();
            }
        } catch (ApplicationError applicationError) {
            LOG.error(applicationError);
            return Response.status(503).entity(applicationError.toString()).build();
        }
    }
}
