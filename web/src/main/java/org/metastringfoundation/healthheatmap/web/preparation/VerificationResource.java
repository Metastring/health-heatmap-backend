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

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.metastringfoundation.data.DatasetIntegrityError;
import org.metastringfoundation.datareader.dataset.table.Table;
import org.metastringfoundation.datareader.dataset.table.TableDescription;
import org.metastringfoundation.datareader.dataset.table.csv.CSVTable;
import org.metastringfoundation.healthheatmap.logic.Application;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("verify")
public class VerificationResource {
    @Inject
    Application app;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public VerificationResult verify(MultipartFormDataInput input) throws IOException, DatasetIntegrityError {
        Table table = new CSVTable(input.getFormDataMap().get("file").get(0).getBodyAsString());
        List<TableDescription> descriptions = new ArrayList<>();
        for (String filename : List.of("metadata1", "metadata2")) {
            String bodyAsString = getBodyAsString(input, filename);
            if (bodyAsString == null) {
                continue;
            }
            TableDescription tableDescription = TableDescription.fromString(bodyAsString);
            descriptions.add(tableDescription);
        }
        return new VerificationResult(app.verify(table, descriptions));
    }
    private @Nullable
    String getBodyAsString(MultipartFormDataInput input, String filename) {
        try {
            return input.getFormDataMap().get(filename).get(0).getBodyAsString();
        } catch (IndexOutOfBoundsException | IOException e) {
            return null;
        }
    }
}

