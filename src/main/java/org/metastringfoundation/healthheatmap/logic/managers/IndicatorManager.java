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
import org.metastringfoundation.healthheatmap.storage.HibernateManager;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

public class IndicatorManager extends DimensionManager {
    public static List<Indicator> getAllIndicators() {
        EntityManager entityManager = HibernateManager.openEntityManager();
        TypedQuery<Indicator> query = entityManager.createNamedQuery("Indicator.findAll", Indicator.class);
        List<Indicator> result = query.getResultList();
        entityManager.close();
        return result;
    }

    public static Indicator addIndicator(String name, EntityManager entityManager) {
        Indicator indicator = new Indicator();
        indicator.setCanonicalName(name);
        entityManager.persist(indicator);
        return indicator;
    }

    public static Indicator findById(Long id, EntityManager entityManager) {
        return HibernateManager.namedQuerySingle(
                Indicator.class,
                "Indicator.findById",
                Collections.singletonMap("id", id),
                entityManager
        );
    }

    public static List<Indicator> findByName(String name, EntityManager entityManager) {
        return HibernateManager.namedQueryList(
                Indicator.class,
                "Indicator.findByName",
                Collections.singletonMap("name", name),
                entityManager
                );
    }

    private static List<Indicator> findIndicatorByNameCreatingIfNotExists(String name, EntityManager entityManager) {
        List<Indicator> indicators = findByName(name, entityManager);
        if (indicators.size() == 0) {
            Indicator indicator = addIndicator(name, entityManager);
            indicators.add(indicator);
        }
        return indicators;
    }

    public static Indicator findIndicatorFromUnmatchedIndicator(UnmatchedIndicator indicator, EntityManager entityManager) throws UnknownEntityError, AmbiguousEntityError {
        String name = indicator.getName();
        List<Indicator> indicatorList = findIndicatorByNameCreatingIfNotExists(name, entityManager);

        if (indicatorList.size() > 1) {
            throw new AmbiguousEntityError("More than one indicator found by name " + name);
        }
        return indicatorList.get(0);
    }
}
