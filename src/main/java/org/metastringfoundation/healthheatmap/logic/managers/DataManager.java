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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.entities.DataElement;

import javax.persistence.TypedQuery;
import java.util.List;

public class DataManager extends DimensionManager {
    private static final Logger LOG = LogManager.getLogger(DataManager.class);

    public static List<DataElement> getAllData(Long indicatorId, Long geographyId, Long sourceId) {
        TypedQuery<DataElement> query = persistenceManager.createQuery("" +
                "select d from DataElement d " +
                "JOIN d.indicator i " +
                "JOIN d.geography g " +
                "JOIN d.source s " +
                "WHERE i.id = :indicatorId " +
                "AND s.id = :sourceId " +
                "AND g.id = :geographyId", DataElement.class);
        return query
                .setParameter("indicatorId", indicatorId)
                .setParameter("geographyId", geographyId)
                .setParameter("sourceId", sourceId)
                .getResultList();
    }
}
