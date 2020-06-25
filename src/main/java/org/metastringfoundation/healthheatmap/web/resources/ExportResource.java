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
import org.metastringfoundation.healthheatmap.logic.KeyValuePairsToCSV;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Path("export")
public class ExportResource {
    private static final Logger LOG = LogManager.getLogger(ExportResource.class);
    private final Application app;

    @Inject
    public ExportResource(Application app) {
        this.app = app;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String exportAnyFieldsAsCSV(@QueryParam("fields") List<String> fields) throws IOException {
        List<Map<String, Object>> fieldCombos = app.getAllTermsOfFields(fields);
        return KeyValuePairsToCSV.convertToCSVWithFirstElementKeysAsHeaders(fieldCombos);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> exportAnyField(@QueryParam("fields") List<String> fields) throws IOException {
        LOG.debug("Fetching " + fields);
        List<Map<String, Object>> result = app.getAllTermsOfFields(fields);
        LOG.debug(result);
        return result;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> exportAnyFieldAdvanced(List<String> fields) throws IOException {
        LOG.debug("Fetching " + fields);
        List<Map<String, Object>> result = app.getAllTermsOfFields(fields);
        LOG.debug(result);
        return result;
    }
}
