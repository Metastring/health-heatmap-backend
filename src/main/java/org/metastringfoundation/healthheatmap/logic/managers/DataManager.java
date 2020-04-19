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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataManager extends DimensionManager {
    private static final Logger LOG = LogManager.getLogger(DataManager.class);

    public static List<String> splitStringToArray(String input, String delimitor) {
        if (input.contains(delimitor)) {
            return Arrays.asList(input.split(delimitor));
        } else {
            return Collections.singletonList(input);
        }
    }

    public static List<Long> stringArrayToLong(List<String> input) {
        return input.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public static List<DataElement> getAllData(String indicatorGroups, String indicatorSubGroups, String indicators, String geographies, String geographyTypes, String sources) {
        List<String> indicatorGroupNames;
        if (indicatorGroups != null) {
            indicatorGroupNames = splitStringToArray(indicatorGroups, ",");
        }
        List<Long> indicatorIds = null;
        if (indicators != null) {
            indicatorIds = stringArrayToLong(splitStringToArray(indicators, ","));
        }
        List<Long> geographyIds = null;
        if (geographies != null) {
            geographyIds = stringArrayToLong(splitStringToArray(geographies, ","));
        }
        List<Long> sourceIds = null;
        if (sources != null) {
            sourceIds = stringArrayToLong(splitStringToArray(sources, ","));
        }
        List<String> joins = new ArrayList<>();
        List<String> wheres = new ArrayList<>();
        if (indicatorIds != null) {
            joins.add("JOIN d.indicator i");
            wheres.add("i.id IN :indicatorIds");
        }
        if (geographyIds != null) {
            joins.add("JOIN d.geography g");
            wheres.add("g.id IN :geographyIds");
        }
        if (sourceIds != null) {
            joins.add("JOIN d.source s");
            wheres.add("s.id IN :sourceIds");
        }
        String queryString = "SELECT d from DataElement d ";
        if (joins.size() > 0) {
            queryString = queryString + String.join(" ", joins);
            queryString = queryString + " WHERE ";
            queryString = queryString + String.join(" AND ", wheres);
            LOG.debug("Query formed is: " + queryString);
        }
        TypedQuery<DataElement> query = persistenceManager.createQuery(queryString, DataElement.class);
        if (indicatorIds != null) {
            query.setParameter("indicatorIds", indicatorIds);
        }
        if (geographyIds != null) {
            query.setParameter("geographyIds", geographyIds);
        }
        if (sourceIds != null) {
            query.setParameter("sourceIds", sourceIds);
        }
        return query.getResultList();
    }
}
