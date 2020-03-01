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

import org.metastringfoundation.healthheatmap.pojo.Indicator;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class IndicatorManager {
    private static IndicatorManager indicatorManager;

    public static IndicatorManager getInstance() {
        if (indicatorManager != null) {
            return indicatorManager;
        }
        indicatorManager = new IndicatorManager();
        return indicatorManager;
    }

    private EntityManager persistenceManager;

    public void setPersistenceManager(EntityManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    private List<Indicator> indicatorList;

    private IndicatorManager() {
    }

    public void setIndicatorList(List<Indicator> indicatorList) {
        this.indicatorList = indicatorList;
    }

    public Indicator addIndicator(String name) {
        Indicator indicator = new Indicator();
        indicator.setCanonicalName(name);
        persistenceManager.getTransaction().begin();
        persistenceManager.persist(indicator);
        persistenceManager.getTransaction().commit();
        return indicator;
    }

    public List<Indicator> getIndicators () {
        return indicatorList;
    }
}
