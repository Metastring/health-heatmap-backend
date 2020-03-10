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

package org.metastringfoundation.healthheatmap.logic;

import org.metastringfoundation.healthheatmap.dataset.UnmatchedReport;
import org.metastringfoundation.healthheatmap.pojo.Report;

import javax.persistence.TypedQuery;
import java.net.URI;
import java.util.List;

public class ReportManager extends DimensionManager {
    private static List<Report> findReportByUri(URI uri) {
        TypedQuery<Report> query = persistenceManager.createNamedQuery("Report.findByUri", Report.class);
        query.setParameter("uri", uri);
        return query.getResultList();
    }

    public static Report addReport(URI uri) {
        Report report = new Report();
        report.setUri(uri);
        persistenceManager.persist(report);
        return report;
    }

    public static Report findReportFromUnmatchedReport(UnmatchedReport report) throws AmbiguousEntityError {
        URI uri = report.getUri();
        List<Report> reportList = findReportByUri(uri);

        if (reportList.size() > 1) {
            throw new AmbiguousEntityError("More than one report added at that uri:  " + uri);
        }

        if (reportList.size() == 0) {
            Report createdReport = addReport(uri);
            reportList.add(createdReport);
        }

        return reportList.get(0);
    }
}
