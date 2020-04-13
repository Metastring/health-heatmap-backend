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
import org.metastringfoundation.healthheatmap.dataset.Dataset;
import org.metastringfoundation.healthheatmap.dataset.Table;
import org.metastringfoundation.healthheatmap.entities.DataElement;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;
import org.metastringfoundation.healthheatmap.entities.Geography;
import org.metastringfoundation.healthheatmap.entities.Indicator;
import org.metastringfoundation.healthheatmap.storage.Database;
import org.metastringfoundation.healthheatmap.storage.HibernateManager;
import org.metastringfoundation.healthheatmap.storage.PostgreSQL;
import org.metastringfoundation.healthheatmap.web.ResponseTypes.AggregatedData;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * One (and only) implementation of the application that acutally does the hard work of wiring everything together.
 * Brings everything else together to make web resources work, CLI, and anything else that needs to work.
 */
public class DefaultApplication implements Application {

    private static final Logger LOG = LogManager.getLogger(DefaultApplication.class);

    public static final Database psql;

    static {
        try {
            psql = new PostgreSQL();
        } catch (ApplicationError applicationError) {
            applicationError.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static final EntityManager persistenceManager = HibernateManager.openEntityManager();
    public static final RestHighLevelClient elastic = new RestHighLevelClient(RestClient.builder(
            new HttpHost("localhost", 9200, "http")
    ));

    public void shutDown() throws ApplicationError {
        try {
            elastic.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HibernateManager.closeEntityManagerFactory();
        try {
            psql.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new ApplicationError("Closing psql errored");
        }
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
        List<Indicator> indicatorList = IndicatorManager.getAllIndicators();
        return jsonizeList(indicatorList);
    }

    @Override
    public String getEntities() throws ApplicationError {
        List<Geography> geographyList = GeographyManager.getAllGeographies();
        return jsonizeList(geographyList);
    }

    @Override
    public String addIndicator(String indicatorName) throws ApplicationError {
        Indicator indicator = IndicatorManager.addIndicator(indicatorName);
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

    @Override
    public String getData(Long indicatorId, Long geographyId, String aggregation) throws ApplicationError {
        LOG.info("fetching data from application");
        List<DataElement> dataElements = DataManager.getAllData(indicatorId, geographyId);
        AggregatedData result = new AggregatedData();
        result.setData(dataElements);
        result.setAggregation(Aggregator.getAverage(dataElements).toString());
        return jsonizeObject(result);
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

    public Long saveDataset(Dataset dataset) throws ApplicationError {
        return DatasetUploader.upload(dataset);
    }

    public void saveTable(String name, Table table) throws ApplicationError {
        TableBackup.backup(name, table);
    }
}
