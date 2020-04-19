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

package org.metastringfoundation.healthheatmap.storage;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

public class HibernateManager {

    private static EntityManagerFactory entityManagerFactory = null;

    public static EntityManager openEntityManager() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory( "org.metastringfoundation.healthheatmap.persistence.truth" );
        }
        return entityManagerFactory.createEntityManager();
    }

    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    public static <C> List<C> loadAllOfType(EntityManager persistenceManager, Class<C> type) {
        CriteriaBuilder criteriaBuilder = persistenceManager.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = criteriaBuilder.createQuery(type);
        Root<C> rootEntry = criteriaQuery.from(type);
        CriteriaQuery<C> allEntities = criteriaQuery.select(rootEntry);

        TypedQuery<C> allQuery = persistenceManager.createQuery(allEntities);
        return allQuery.getResultList();
    }

    public static <C> List<C> namedQueryList(Class<C> type, String queryName, Map<String, ?> params) {
        EntityManager entityManager = openEntityManager();
        TypedQuery<C> query = entityManager.createNamedQuery(queryName, type);
        if (!(params == null || params.isEmpty())) {
            for (String key: params.keySet()) {
                query.setParameter(key, params.get(key));
            }
        }
        List<C> result = query.getResultList();
        entityManager.close();
        return result;
    }

    public static <C> C namedQuerySingle(Class<C> type, String queryName, Map<String, ?> params) {
        EntityManager entityManager = openEntityManager();
        TypedQuery<C> query = entityManager.createNamedQuery(queryName, type);
        if (!(params.isEmpty())) {
            for (String key: params.keySet()) {
                query.setParameter(key, params.get(key));
            }
        }
        C result = query.getSingleResult();
        entityManager.close();
        return result;
    }

    public static void persist(Object o) {
        EntityManager entityManager = openEntityManager();
        entityManager.persist(o);
        entityManager.close();
    }

    public static <C> C find(Class<C> type, Long id) {
        EntityManager entityManager = openEntityManager();
        C result = entityManager.find(type, id);
        entityManager.close();
        return result;
    }
}
