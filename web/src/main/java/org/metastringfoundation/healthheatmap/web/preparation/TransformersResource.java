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

package org.metastringfoundation.healthheatmap.web.preparation;

import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.etl.KeyValuePairsToCSV;
import org.metastringfoundation.healthheatmap.web.preparation.beans.TransformersInfoBean;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Path("transformer")
public class TransformersResource {
    private final Application app;

    @Inject
    public TransformersResource(Application app) {
        this.app = app;
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getListOfTransformers() {
        return app.getListOfTransformers();
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public TransformersInfoBean getTransformerDetails(@QueryParam("name") String transformerName) {
        Map<Map<String, String>, List<Map<String, String>>> rules = app.getTransformerRules(transformerName);
        List<Map<String, String>> failures = app.getTransformerFailures(transformerName);
        return new TransformersInfoBean(rules, failures);
    }

    @GET
    @Path("failures")
    @Produces("text/csv")
    public Response getFailuresAsCSV(@QueryParam("name") String transformerName) throws IOException {
        List<Map<String, String>> failures = app.getTransformerFailures(transformerName);
        String resultCSV = KeyValuePairsToCSV.convertToCSVWithFirstElementKeysAsHeaders(failures);
        return Response
                .ok(resultCSV)
                .header("Content-Disposition", "attachment; filename=\"transformer-failures.csv\"")
                .build();
    }
}
