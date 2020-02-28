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
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;
import org.metastringfoundation.healthheatmap.pojo.Entity;
import org.metastringfoundation.healthheatmap.pojo.Indicator;
import org.metastringfoundation.healthheatmap.storage.Database;
import org.metastringfoundation.healthheatmap.storage.HibernateManager;
import org.metastringfoundation.healthheatmap.storage.PostgreSQL;

import java.util.List;

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
        indicatorManager.setPersistenceManager(persistenceManager);
    }

    private static final IndicatorManager indicatorManager = IndicatorManager.getInstance();
    private static final EntityManager entityManager = EntityManager.getInstance();

    public DefaultApplication() {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9201, "http")
        ));
        setElastic(client);

        Database psql = new PostgreSQL();
        setPsql(psql);

        javax.persistence.EntityManager persistenceManager = HibernateManager.openEntityManager();
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
        List<Indicator> indicatorList = indicatorManager.getIndicators();
        return jsonizeList(indicatorList);
    }

    @Override
    public String getEntities() throws ApplicationError {
        List<Entity> entityList = entityManager.getEntities();
        return jsonizeList(entityList);
    }

    @Override
    public String addIndicator(String indicatorName) throws ApplicationError {
        Indicator indicator = indicatorManager.addIndicator(indicatorName);
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
}
