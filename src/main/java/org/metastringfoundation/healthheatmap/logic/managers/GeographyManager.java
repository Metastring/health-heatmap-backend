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
import org.metastringfoundation.healthheatmap.dataset.entities.UnmatchedGeography;
import org.metastringfoundation.healthheatmap.entities.Geography;
import org.metastringfoundation.healthheatmap.logic.errors.AmbiguousEntityError;
import org.metastringfoundation.healthheatmap.logic.errors.UnknownEntityError;
import org.metastringfoundation.healthheatmap.storage.HibernateManager;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeographyManager extends DimensionManager {
    private static final Logger LOG = LogManager.getLogger(GeographyManager.class);

    public static List<Geography> getAllGeographies() {
        return HibernateManager.namedQueryList(
                Geography.class,
                "Geography.findAll",
                null
        );
    }

    public static List<Geography> getGeographyByType(String type) {
        if (type.equals("ANY")) return getAllGeographies();
        return HibernateManager.namedQueryList(
                Geography.class,
                "Geography.findByType",
                Collections.singletonMap("type", Geography.GeographyType.valueOf(type))
        );
    }

    public static List<Geography> findByName(String name, EntityManager entityManager) {
        return HibernateManager.namedQueryList(
                Geography.class,
                "Geography.findByName",
                Collections.singletonMap("name", name),
                entityManager
        );
    }

    public static List<Geography> findChildByName(String name, Geography belongsTo, EntityManager entityManager) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("parent", belongsTo);
        return HibernateManager.namedQueryList(
                Geography.class,
                "Geography.findChild",
                params,
                entityManager
        );
    }

    public static Geography findById(Long id) {
        return HibernateManager.find(Geography.class, id);
    }

    private static List<Geography> findDistrictByNameCreatingIfNotExists(String name, Geography belongsTo, EntityManager entityManager) {
        List<Geography> geographies = findChildByName(name, belongsTo, entityManager);
        if (geographies.size() == 0) {
            Geography geography = createGeography(name, belongsTo, Geography.GeographyType.DISTRICT, entityManager);
            geographies.add(geography);
        }
        return geographies;
    }

    private static List<Geography> findStateByNameCreatingIfNotExists(String name, EntityManager entityManager) {
        List<Geography> geographies = findByName(name, entityManager);
        if (geographies.size() == 0) {
            Geography geography = createGeography(name, null, Geography.GeographyType.STATE, entityManager);
            geographies.add(geography);
        }
        return geographies;
    }

    public static Geography createGeography(String name, Geography belongsTo, Geography.GeographyType type, EntityManager entityManager) {
        Geography geography = new Geography();
        geography.setCanonicalName(name);
        geography.setType(type);
        geography.setBelongsTo(belongsTo);
        entityManager.persist(geography);
        return geography;
    }

    public static Geography findGeographyFromUnmatchedGeography(UnmatchedGeography geography, EntityManager entityManager) throws UnknownEntityError, AmbiguousEntityError {
        String state = geography.getState();
        List<Geography> stateGeographyList = findStateByNameCreatingIfNotExists(state, entityManager);
        if (stateGeographyList.size() > 1) {
            LOG.debug(stateGeographyList);
            throw new AmbiguousEntityError("More than one state by the name " + state + ". Please pass more specificiers");
        }
        Geography stateGeography = stateGeographyList.get(0);

        String district = geography.getDistrict();
        if (district != null) {
            List<Geography> districtGeographyList = findDistrictByNameCreatingIfNotExists(district, stateGeography, entityManager);
            if (districtGeographyList.size() > 1) {
                throw new AmbiguousEntityError("More than one district in state " + state + " by name " + district);
            }
            return districtGeographyList.get(0);
        } else {
            return stateGeography;
        }
    }
}
