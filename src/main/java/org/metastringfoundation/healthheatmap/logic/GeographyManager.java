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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.dataset.UnmatchedGeography;
import org.metastringfoundation.healthheatmap.entities.Geography;

import javax.persistence.TypedQuery;
import java.util.List;

public class GeographyManager extends DimensionManager {
    private static Logger LOG = LogManager.getLogger(GeographyManager.class);

    public static List<Geography> getAllGeographies() {
        TypedQuery<Geography> query = persistenceManager.createNamedQuery("Geography.findAll", Geography.class);
        return query.getResultList();
    }

    public static List<Geography> findByName(String name) {
        TypedQuery<Geography> query = persistenceManager.createNamedQuery("Geography.findByName", Geography.class);
        query.setParameter("name", name);
        return query.getResultList();
    }

    public static List<Geography> findChildByName(String name, Geography belongsTo) {
        TypedQuery<Geography> query = persistenceManager.createNamedQuery("Geography.findChild", Geography.class);
        query.setParameter("name", name);
        query.setParameter("parent", belongsTo);
        return query.getResultList();
    }

    public static Geography findById(Long id) {
        return persistenceManager.find(Geography.class, id);
    }

    private static List<Geography> findDistrictByNameCreatingIfNotExists(String name, Geography belongsTo) {
        List<Geography> geographies = findChildByName(name, belongsTo);
        if (geographies.size() == 0) {
            Geography geography = createGeography(name, belongsTo, Geography.GeographyType.DISTRICT);
            geographies.add(geography);
        }
        return geographies;
    }

    private static List<Geography> findStateByNameCreatingIfNotExists(String name) {
        List<Geography> geographies = findByName(name);
        if (geographies.size() == 0) {
            Geography geography = createGeography(name, null, Geography.GeographyType.STATE);
            geographies.add(geography);
        }
        return geographies;
    }

    public static Geography createGeography(String name, Geography belongsTo, Geography.GeographyType type) {
        Geography geography = new Geography();
        geography.setCanonicalName(name);
        geography.setType(type);
        persistenceManager.persist(geography);
        return geography;
    }

    public static Geography findGeographyFromUnmatchedGeography(UnmatchedGeography geography) throws UnknownEntityError, AmbiguousEntityError {
        String state = geography.getState();
        List<Geography> stateGeographyList = findStateByNameCreatingIfNotExists(state);
        if (stateGeographyList.size() > 1) {
            LOG.debug(stateGeographyList);
            throw new AmbiguousEntityError("More than one state by the name " + state + ". Please pass more specificiers");
        }
        Geography stateGeography = stateGeographyList.get(0);

        String district = geography.getDistrict();
        if (district != null) {
            List<Geography> districtGeographyList = findDistrictByNameCreatingIfNotExists(district, stateGeography);
            if (districtGeographyList.size() > 1) {
                throw new AmbiguousEntityError("More than one district in state " + state + " by name " + district);
            }
            return districtGeographyList.get(0);
        } else {
            return stateGeography;
        }
    }
}
