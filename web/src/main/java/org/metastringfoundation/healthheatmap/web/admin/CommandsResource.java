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

package org.metastringfoundation.healthheatmap.web.admin;

import org.metastringfoundation.healthheatmap.logic.Application;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("reload")
public class CommandsResource {
    private final Application app;

    @Inject
    public CommandsResource(Application app) {
        this.app = app;
    }

    @POST
    @Path("dimensions")
    public void reloadDimensionAssociations() throws IOException {
        app.reloadMemoryStores();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("debug/getAssociations")
    public List<String> getAllAssociations() {
        return app.getAllIndicatorsWithAssociations();
    }
}
