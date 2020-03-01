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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("entities")
public class GeographyResource {
    private static final Logger LOG = LogManager.getLogger(GeographyResource.class);

    private static final String ZOOM_LEVEL = "zoom";
    private static final String LOAD_ENTITY_TYPE = "type";

    private static final String DEFAULT_ZOOM_LEVEL = "0";
    private static final String DEFAULT_LOAD_ENTITY_TYPE = "DISTRICT";


    @Inject
    Application app;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEntities(
    ) {
        try {
            return Response.status(200).entity(app.getIndicators()).build();
        } catch (ApplicationError applicationError) {
            LOG.error(applicationError);
            return Response.status(503).entity(applicationError.toString()).build();
        }
    }
}
