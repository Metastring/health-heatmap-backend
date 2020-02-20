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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.helpers.Jsonizer;
import org.metastringfoundation.healthheatmap.pojo.Indicator;
import org.metastringfoundation.healthheatmap.storage.Database;

import java.util.List;

public class DefaultApplication implements Application {
    private static final Logger LOG = LogManager.getLogger(DefaultApplication.class);

    private Database psql;

    public void setPsql(Database psql) {
        this.psql = psql;
    }

    private static final IndicatorManager indicatorManager = new IndicatorManager();

    @Override
    public String getIndicators() throws ApplicationError {
        List<Indicator> indicatorList = indicatorManager.getIndicators();
        try {
            return Jsonizer.getJSONString(indicatorList);
        } catch (JsonProcessingException e) {
            LOG.error(e);
            throw new ApplicationError(e);
        }
    }

    @Override
    public String getEntities() {
        return "someentities";
    }

    @Override
    public String saveIndicator(String indicatorJSON) {
        return "saved the indicator " + indicatorJSON;
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
