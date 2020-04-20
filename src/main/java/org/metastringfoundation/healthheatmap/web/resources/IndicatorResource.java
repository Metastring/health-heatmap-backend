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
import org.metastringfoundation.healthheatmap.entities.Indicator;
import org.metastringfoundation.healthheatmap.logic.Application;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("indicators")
public class IndicatorResource {
    private static final Logger LOG = LogManager.getLogger(IndicatorResource.class);


    @Inject
    Application app;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Indicator> getIndicators() {
            return app.getIndicators();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Indicator addIndicator(
            @QueryParam("name") String name
    ) {
        return app.addIndicator(name);
    }
}