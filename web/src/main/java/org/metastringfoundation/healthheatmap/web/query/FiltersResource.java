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

import org.metastringfoundation.healthheatmap.beans.Filter;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.web.utils.ErrorCreator;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("filters")
public class FiltersResource {
    private final Application app;

    @Inject
    public FiltersResource(Application app) {
        this.app = app;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<String>> getFiltersForIndicator(@QueryParam("indicator") String indicator) {
        return app.getFieldsAssociatedWithIndicator(indicator);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<String>> getDimensionFiltersPossibleAt(Filter filter) throws IOException {
        if (filter == null || (filter.getTerms() == null && filter.getRanges() == null)) { // NOPMD readability
            throw new WebApplicationException(ErrorCreator.getPublicViewableError("Must specify filter at which dimensions should be given out"));
        }
        return app.getFieldsPossibleAt(filter).entrySet().stream()
                .filter(e -> !e.getKey().equals("entity.id"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
