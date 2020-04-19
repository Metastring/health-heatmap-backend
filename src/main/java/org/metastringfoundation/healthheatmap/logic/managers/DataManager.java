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
import org.metastringfoundation.healthheatmap.entities.IndicatorGroup;

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
        TypedQuery<DataElement> query = persistenceManager.createQuery("" +
                "select d from DataElement d " +
                "JOIN d.indicator i " +
                "JOIN d.geography g " +
                "JOIN d.source s " +
                "WHERE i.id IN :indicatorIds " +
                "AND s.id IN :sourceIds " +
                "AND g.id IN :geographyIds", DataElement.class);
        return query
                .setParameter("indicatorIds", indicatorIds)
                .setParameter("geographyIds", geographyIds)
                .setParameter("sourceIds", sourceIds)
                .getResultList();
    }
}
