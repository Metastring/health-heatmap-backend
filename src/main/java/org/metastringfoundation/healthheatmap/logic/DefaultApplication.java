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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.metastringfoundation.healthheatmap.dataset.*;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;
import org.metastringfoundation.healthheatmap.pojo.*;
import org.metastringfoundation.healthheatmap.storage.Database;
import org.metastringfoundation.healthheatmap.storage.HibernateManager;
import org.metastringfoundation.healthheatmap.storage.PostgreSQL;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * One (and only) implementation of the application that acutally does the hard work of wiring everything together.
 * Brings everything else together to make web resources work, CLI, and anything else that needs to work.
 */
public class DefaultApplication implements Application {

    private static final Logger LOG = LogManager.getLogger(DefaultApplication.class);

    private static Database psql;
    private static RestHighLevelClient elastic;
    private static javax.persistence.EntityManager persistenceManager;

    private static void setElastic(RestHighLevelClient restHighLevelClient) {
        elastic = restHighLevelClient;
    }

    private static void setPsql(Database database) {
        psql = database;
    }

    public static void setPersistenceManager(javax.persistence.EntityManager persistenceManager) {
        DefaultApplication.persistenceManager = persistenceManager;
        INDICATOR_MANAGER.setPersistenceManager(persistenceManager);
        GEOGRAPHY_MANAGER.setPersistenceManager(persistenceManager);
        SOURCE_MANAGER.setPersistenceManager(persistenceManager);
    }

    private static final IndicatorManager INDICATOR_MANAGER = IndicatorManager.getInstance();
    private static final GeographyManager GEOGRAPHY_MANAGER = GeographyManager.getInstance();
    private static final SourceManager SOURCE_MANAGER = SourceManager.getInstance();

    public DefaultApplication() {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ));
        setElastic(client);

        Database psql = new PostgreSQL();
        setPsql(psql);

        EntityManager persistenceManager = HibernateManager.openEntityManager();
        setPersistenceManager(persistenceManager);

    }

    public DefaultApplication(RestHighLevelClient restHighLevelClient, Database psql) {
        setElastic(restHighLevelClient);
        setPsql(psql);
    }

    private String jsonizeList(List<?> objectList) throws ApplicationError{
        try {
            return Jsonizer.getJSONString(objectList);
        } catch (JsonProcessingException e) {
            LOG.error(e);
            throw new ApplicationError(e);
        }
    }

    private String jsonizeObject(Object object) throws ApplicationError {
        try {
            return Jsonizer.asJSON(object);
        } catch (JsonProcessingException e) {
            LOG.error(e);
            throw new ApplicationError(e);
        }
    }

    @Override
    public String getIndicators() throws ApplicationError {
        List<Indicator> indicatorList = INDICATOR_MANAGER.getAllIndicators();
        return jsonizeList(indicatorList);
    }

    @Override
    public String getEntities() throws ApplicationError {
        List<Geography> geographyList = GEOGRAPHY_MANAGER.getAllGeographies();
        return jsonizeList(geographyList);
    }

    @Override
    public String addIndicator(String indicatorName) throws ApplicationError {
        Indicator indicator = INDICATOR_MANAGER.addIndicator(indicatorName);
        return jsonizeObject(indicator);
    }

    @Override
    public String saveEntity(String entityJSON) {
        return null;
    }

    @Override
    public String getDimension(String dimension) {
        return null;
    }

    public String getHealth() {
        return psql.getHealth();
    }

    private void beginTransaction() {
        persistenceManager.getTransaction().begin();
    }

    private void commitTransaction() {
        persistenceManager.getTransaction().commit();
    }

    public void saveDataset(Dataset dataset) {
        Map<UnmatchedGeography, Geography> geographyEntityMap = new HashMap<>();
        Map<UnmatchedIndicator, Indicator> indicatorMap = new HashMap<>();
        Map<UnmatchedSettlement, Settlement> settlementMap = new HashMap<>();

        // TODO: Read this from metadata
        UnmatchedSource unmatchedSource = new UnmatchedSource();
        unmatchedSource.setName("NFHS");

        Source source = null;
        try {
            source = SOURCE_MANAGER.findSourceFromUnmatchedSource(unmatchedSource);
        } catch (AmbiguousEntityError ambiguousEntityError) {
            LOG.error(ambiguousEntityError);
        }


        beginTransaction();

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
                        geography = GEOGRAPHY_MANAGER.findGeographyFromUnmatchedGeography(unmatchedGeography);
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
                        indicator = INDICATOR_MANAGER.findIndicatorFromUnmatchedIndicator(unmatchedIndicator);
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
            dataElement.setSource(source);

            dataElement.setValue(unmatchedDataElement.getValue());

            persistenceManager.persist(dataElement);
        }

        commitTransaction();
    }
}
