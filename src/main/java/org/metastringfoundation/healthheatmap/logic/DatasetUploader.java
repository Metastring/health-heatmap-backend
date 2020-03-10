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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.dataset.*;
import org.metastringfoundation.healthheatmap.pojo.*;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

public class DatasetUploader {
    private static final Logger LOG = LogManager.getLogger(DatasetUploader.class);

    private static final EntityManager persistenceManager = DefaultApplication.persistenceManager;

    public static void upload(Dataset dataset) throws ApplicationError {
        Map<UnmatchedGeography, Geography> geographyEntityMap = new HashMap<>();
        Map<UnmatchedIndicator, Indicator> indicatorMap = new HashMap<>();
        Map<UnmatchedSettlement, Settlement> settlementMap = new HashMap<>();

        persistenceManager.getTransaction().begin();

        LOG.debug(dataset.getMetadata());

        UnmatchedSource unmatchedSource = dataset.getMetadata().getSource();
        UnmatchedReport unmatchedReport = dataset.getMetadata().getReport();

        Source source;
        Report report;
        Upload upload;

        try {
            source = SourceManager.findSourceFromUnmatchedSource(unmatchedSource);
        } catch (AmbiguousEntityError ex) {
            throw new ApplicationError("Impossible to find source");
        }

        try {
            report = ReportManager.findReportFromUnmatchedReport(unmatchedReport);
        } catch (AmbiguousEntityError ambiguousEntityError) {
            throw new ApplicationError("No report");
        }

        upload = UploadManager.newUpload(report, source);

        for (UnmatchedDataElement unmatchedDataElement: dataset.getData()) {
            DataElement dataElement = new DataElement();
            Geography geography = null;
            Indicator indicator = null;
            Settlement settlement = null;

            UnmatchedGeography unmatchedGeography = unmatchedDataElement.getGeography();
            if (unmatchedGeography != null) {
                Geography geographyFromMap = geographyEntityMap.get(unmatchedGeography);
                if (geographyFromMap != null) {
                    geography = geographyFromMap;
                } else {
                    try {
                        geography = GeographyManager.findGeographyFromUnmatchedGeography(unmatchedGeography);
                        geographyEntityMap.put(unmatchedGeography, geography);
                    } catch (AmbiguousEntityError | UnknownEntityError ex) {
                        LOG.error(ex);
                    }
                }
            }

            UnmatchedIndicator unmatchedIndicator = unmatchedDataElement.getIndicator();
            if (unmatchedIndicator != null) {
                Indicator indicatorFromMap = indicatorMap.get(unmatchedIndicator);
                if (indicatorFromMap != null) {
                    indicator = indicatorFromMap;
                } else {
                    try {
                        indicator = IndicatorManager.findIndicatorFromUnmatchedIndicator(unmatchedIndicator);
                        indicatorMap.put(unmatchedIndicator, indicator);
                    } catch (AmbiguousEntityError | UnknownEntityError ex) {
                        LOG.error(ex);
                    }
                }
            }

            UnmatchedSettlement unmatchedSettlement = unmatchedDataElement.getSettlement();
            if (unmatchedSettlement != null) {
                Settlement settlementFromMap = settlementMap.get(unmatchedSettlement);
                if (settlementFromMap != null) {
                    settlement = settlementFromMap;
                } else {
                    try {
                        settlement = new Settlement();
                        Settlement.SettlementType settlementType = Settlement.getSettlementTypeFromString(unmatchedSettlement.getType());
                        settlement.setSettlement(settlementType);
                        settlementMap.put(unmatchedSettlement, settlement);
                    } catch (IllegalArgumentException ex) {
                        LOG.error(ex);
                    }
                }
            }


            dataElement.setGeography(geography);
            dataElement.setIndicator(indicator);
            dataElement.setSettlement(settlement);

            dataElement.setUpload(upload);
            dataElement.setSource(source);
            dataElement.setReport(report);

            dataElement.setValue(unmatchedDataElement.getValue());

            persistenceManager.persist(dataElement);
        }

        persistenceManager.getTransaction().commit();
    }
}
