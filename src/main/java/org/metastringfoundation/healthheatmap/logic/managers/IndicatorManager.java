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

package org.metastringfoundation.healthheatmap.logic.managers;

import org.metastringfoundation.healthheatmap.dataset.entities.UnmatchedIndicator;
import org.metastringfoundation.healthheatmap.entities.Indicator;
import org.metastringfoundation.healthheatmap.logic.errors.AmbiguousEntityError;
import org.metastringfoundation.healthheatmap.logic.errors.UnknownEntityError;

import javax.persistence.TypedQuery;
import java.util.List;

public class IndicatorManager extends DimensionManager {
    public static List<Indicator> getAllIndicators() {
        TypedQuery<Indicator> query = persistenceManager.createNamedQuery("Indicator.findAll", Indicator.class);
        return query.getResultList();
    }

    public static Indicator addIndicatorWithCommit(String name) {
        persistenceManager.getTransaction().begin();
        Indicator indicator = addIndicator(name);
        persistenceManager.getTransaction().commit();
        return indicator;
    }

    public static Indicator addIndicator(String name) {
        Indicator indicator = new Indicator();
        indicator.setCanonicalName(name);
        persistenceManager.persist(indicator);
        return indicator;
    }

    public static Indicator findById(Long id) {
        TypedQuery<Indicator> query = persistenceManager.createNamedQuery("Indicator.findById", Indicator.class);
        query.setParameter("id", id);
        return query.getResultList().get(0);
    }

    private static List<Indicator> findByName(String name) {
        TypedQuery<Indicator> query = persistenceManager.createNamedQuery("Indicator.findByName", Indicator.class);
        query.setParameter("name", name);
        return query.getResultList();
    }

    private static List<Indicator> findIndicatorByNameCreatingIfNotExists(String name) {
        List<Indicator> indicators = findByName(name);
        if (indicators.size() == 0) {
            Indicator indicator = addIndicator(name);
            indicators.add(indicator);
        }
        return indicators;
    }

    public static Indicator findIndicatorFromUnmatchedIndicator(UnmatchedIndicator indicator) throws UnknownEntityError, AmbiguousEntityError {
        String name = indicator.getName();
        List<Indicator> indicatorList = findIndicatorByNameCreatingIfNotExists(name);

        if (indicatorList.size() > 1) {
            throw new AmbiguousEntityError("More than one indicator found by name " + name);
        }
        return indicatorList.get(0);
    }
}
