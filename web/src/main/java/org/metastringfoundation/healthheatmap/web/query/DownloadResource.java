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

import org.metastringfoundation.healthheatmap.beanconverters.FilterToDataQuery;
import org.metastringfoundation.healthheatmap.beans.DownloadRequest;
import org.metastringfoundation.healthheatmap.helpers.ListAndMapUtils;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.etl.KeyValuePairsToCSV;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.web.utils.CSVDownload;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Path("download")
public class DownloadResource {
    private final Application app;

    @Inject
    public DownloadResource(Application app) {
        this.app = app;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/csv")
    public Response downloadData(
            DownloadRequest downloadRequest
    ) throws IOException {
        DataQueryResult queryResult = app.query(FilterToDataQuery.convert(downloadRequest.getFilter()));
        app.logDownload(downloadRequest);
        List<Map<String, String>> resultsToSend = filterFieldsIfRequired(downloadRequest, queryResult);
        return CSVDownload.getDownloadCSVResponse(resultsToSend);
    }

    @POST
    @Path("display")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/csv")
    public String displayDownloadData(
            DownloadRequest downloadRequest
    ) throws IOException {
        DataQueryResult queryResult = app.query(FilterToDataQuery.convert(downloadRequest.getFilter()));
        app.logDownload(downloadRequest);
        List<Map<String, String>> resultsToSend = filterFieldsIfRequired(downloadRequest, queryResult);
        return KeyValuePairsToCSV.convertToCSVPreservingAllColumns(resultsToSend);
    }

    private List<Map<String, String>> filterFieldsIfRequired(DownloadRequest downloadRequest, DataQueryResult queryResult) {
        if (downloadRequest.getFilter() != null) {
            return ListAndMapUtils.filterKeys(queryResult.getResult(), downloadRequest.getFields());
        } else {
            return queryResult.getResult();
        }
    }
}
