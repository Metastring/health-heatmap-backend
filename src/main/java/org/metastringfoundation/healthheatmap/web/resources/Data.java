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

import io.swagger.v3.oas.annotations.Parameter;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.web.ResponseTypes.AggregatedData;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("data")
public class Data {
    @Inject
    Application app;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AggregatedData getData(
//            @Parameter(description = "Name of the group from which to fetch all indicators (optional)")
//            @QueryParam("indicatorGroups") String indicatorGroups,
//
//            @Parameter(description = "Name of the sub-group from which to fetch all indicators (optional)")
//            @QueryParam("indicatorSubGroups") String indicatorSubGroups,

            @Parameter(description = "Comma-separated IDs of the indicators to fetch data of (if indicatorGroup is given, that takes precedence)")
            @QueryParam("indicator") String indicators,

            @Parameter(description = "Comma-seaprated IDs of geographies to fetch data of")
            @QueryParam("geography") String geographies,

//            @Parameter(description = "Use DISTRICT or STATE to get info about only districts or states")
//            @QueryParam("geographyTypes") String geographyTypes,

            @Parameter(description = "Comma-separated IDs of sources to fetch data of")
            @QueryParam("source") String sources,

            @QueryParam("aggregation") String aggregation
    ) {
        String indicatorGroups = null;
        String indicatorSubGroups = null;
        String geographyTypes = null;
        return app.getData(indicatorGroups, indicatorSubGroups, indicators, geographies, geographyTypes, sources, aggregation);
    }
}
