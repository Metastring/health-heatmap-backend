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

package org.metastringfoundation.healthheatmap.logic.workers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.dataset.Dataset;
import org.metastringfoundation.healthheatmap.dataset.entities.*;
import org.metastringfoundation.healthheatmap.entities.*;
import org.metastringfoundation.healthheatmap.logic.errors.AmbiguousEntityError;
import org.metastringfoundation.healthheatmap.logic.errors.ApplicationError;
import org.metastringfoundation.healthheatmap.logic.errors.UnknownEntityError;
import org.metastringfoundation.healthheatmap.logic.managers.*;
import org.metastringfoundation.healthheatmap.storage.HibernateManager;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

public class DatasetUploader {
    private static final Logger LOG = LogManager.getLogger(DatasetUploader.class);

    public static Long upload(Dataset dataset) throws ApplicationError {
        Long returnValue;
        Map<UnmatchedGeography, Geography> geographyEntityMap = new HashMap<>();
        Map<UnmatchedIndicator, Indicator> indicatorMap = new HashMap<>();
        Map<UnmatchedSettlement, Settlement> settlementMap = new HashMap<>();
        Map<UnmatchedGender, Gender> genderMap = new HashMap<>();

        EntityManager entityManager = HibernateManager.openEntityManager();
        entityManager.getTransaction().begin();

        LOG.debug(dataset.getMetadata());

        UnmatchedSource unmatchedSource = dataset.getMetadata().getSource();
        UnmatchedReport unmatchedReport = dataset.getMetadata().getReport();

        TimePeriod timePeriod = dataset.getMetadata().getTimePeriod();

        Source source;
        Report report;
        Upload upload;

        try {
            source = SourceManager.findSourceFromUnmatchedSource(unmatchedSource, entityManager);
        } catch (AmbiguousEntityError ex) {
            throw new ApplicationError("Impossible to find source");
        }

        source.setTimePeriod(timePeriod);

        try {
            report = ReportManager.findReportFromUnmatchedReport(unmatchedReport, entityManager);
        } catch (AmbiguousEntityError ambiguousEntityError) {
            throw new ApplicationError("No report");
        }

        upload = UploadManager.newUpload(report, source, entityManager);
        returnValue = upload.getId();

        for (UnmatchedDataElement unmatchedDataElement: dataset.getData()) {
            DataElement dataElement = new DataElement();
            Geography geography = null;
            Indicator indicator = null;
            Settlement settlement = null;
            Gender gender = null;

            UnmatchedGeography unmatchedGeography = unmatchedDataElement.getGeography();
            if (unmatchedGeography != null) {
                Geography geographyFromMap = geographyEntityMap.get(unmatchedGeography);
                if (geographyFromMap != null) {
                    geography = geographyFromMap;
                } else {
                    try {
                        geography = GeographyManager.findGeographyFromUnmatchedGeography(unmatchedGeography, entityManager);
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
                        indicator = IndicatorManager.findIndicatorFromUnmatchedIndicator(unmatchedIndicator, entityManager);
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
            } else {
                settlement = Settlement.getDefault();
            }

            UnmatchedGender unmatchedGender = unmatchedDataElement.getGender();
            if (unmatchedGender != null) {
                Gender genderFromMap = genderMap.get(unmatchedGender);
                if (genderFromMap != null) {
                    gender = genderFromMap;
                } else {
                    try {
                        gender = new Gender();
                        Gender.GenderType genderType = Gender.getGenderTypeFromString(unmatchedGender.getType());
                        gender.setGender(genderType);
                        genderMap.put(unmatchedGender, gender);
                    } catch (IllegalArgumentException ex) {
                        LOG.error(ex);
                    }
                }
            } else {
                gender = Gender.getDefault();
            }


            dataElement.setGeography(geography);
            dataElement.setIndicator(indicator);
            dataElement.setSettlement(settlement);
            dataElement.setGender(gender);

            dataElement.setUpload(upload);
            dataElement.setSource(source);
            dataElement.setReport(report);
            dataElement.setTimePeriod(timePeriod);

            dataElement.setValue(unmatchedDataElement.getValue());

            entityManager.persist(dataElement);
        }

        entityManager.getTransaction().commit();
        entityManager.close();
        return returnValue;
    }
}
