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

package org.metastringfoundation.healthheatmap.web.utils;

import org.metastringfoundation.healthheatmap.beanconverters.FilterToDataQuery;
import org.metastringfoundation.healthheatmap.beans.Filter;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;
import org.metastringfoundation.healthheatmap.helpers.ListAndMapUtils;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AppInteraction {
    private final Application app;

    @Inject
    public AppInteraction(Application app) {
        this.app = app;
    }

    public List<Map<String, String>> getDataForDownload(FilterAndSelectFields filtersAndFields) throws IOException {
        verifyAndFixFiltersCrashingIfInappropriate(filtersAndFields);
        if (filtersAndFields.getFilter().isPresent()) {
            DataQueryResult queryResult = app.query(FilterToDataQuery.convert(filtersAndFields.getFilter().get()));
            List<Map<String, String>> filtered;
            if (filtersAndFields.getFields() != null) {
                filtered = ListAndMapUtils.filterKeys(queryResult.getResult(), filtersAndFields.getFields());
            } else {
                filtered = queryResult.getResult();
            }
            return filtered;
        }
        throw new WebApplicationException(ErrorCreator.getPublicViewableError("The query without filters can lead to extremely large results. Aborting."));
    }

    private void verifyAndFixFiltersCrashingIfInappropriate(FilterAndSelectFields filtersAndFields) {
        if (filtersAndFields.getFilter().isEmpty()) {
            throw new WebApplicationException(ErrorCreator.getPublicViewableError("Should give filters"));
        }
        if (filtersAndFields.getFields() != null && !filtersAndFields.getFields().contains("value")) {
            filtersAndFields.getFields().add("value");
        }
        if (filtersAndFields.getFilter().isEmpty()) {
            throw new WebApplicationException(ErrorCreator.getPublicViewableError("The query without filters can lead to extremely large results. Aborting."));
        }
    }

    public DataQueryResult query(DataQuery query) throws IOException {
        return app.query(query);
    }

    public List<Map<String, String>> getScores(Filter filter, String dimension, List<String> dimensions) throws IOException {
        Map<String, List<String>> possibleDimensions = app.getFieldsPossibleAt(filter);
        if (!dimension.endsWith(".id")) {
            throw ErrorCreator.getErrorFor("Dimensions probably end with .id");
        }
        possibleDimensions.remove(dimension);
        for (Map.Entry<String, List<String>> entry : possibleDimensions.entrySet()) {
            if (entry.getValue().size() > 1) {
                throw ErrorCreator.getErrorFor("Please filter a single value for " + entry.getKey() + " from "
                        + String.join(", ", entry.getValue()));
            }
        }

        return null;
    }
}
