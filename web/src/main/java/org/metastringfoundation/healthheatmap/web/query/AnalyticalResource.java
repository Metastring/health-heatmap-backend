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

import org.metastringfoundation.healthheatmap.web.query.beans.ScoringRequest;
import org.metastringfoundation.healthheatmap.web.utils.AppInteraction;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Path("analysis")
public class AnalyticalResource {
    private final AppInteraction app;

    @Inject
    public AnalyticalResource(AppInteraction app) {
        this.app = app;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("scoring")
    public List<Map<String, String>> getScores(ScoringRequest request) throws IOException {
        return app.getScores(request.filter, request.dimension, request.dimensions);
    }
}
