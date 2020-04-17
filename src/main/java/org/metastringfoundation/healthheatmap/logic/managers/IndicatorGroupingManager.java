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

import org.metastringfoundation.healthheatmap.dataset.table.Table;
import org.metastringfoundation.healthheatmap.entities.Indicator;
import org.metastringfoundation.healthheatmap.entities.IndicatorGroup;
import org.metastringfoundation.healthheatmap.entities.IndicatorGroupHierarchy;
import org.metastringfoundation.healthheatmap.logic.DefaultApplication;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IndicatorGroupingManager {
    private static final EntityManager persistenceManager = DefaultApplication.persistenceManager;

    public static List<IndicatorGroup> findGroupsByName(String name) {
        TypedQuery<IndicatorGroup> query = persistenceManager.createNamedQuery("IndicatorGroup.findByName", IndicatorGroup.class);
        query.setParameter("name", name);
        return query.getResultList();
    }

    public static List<IndicatorGroupHierarchy> findGroupHierarchiesWithAddress(List<String> address) {
        if (address.size() != 6) {
            throw new IllegalArgumentException("Invalid address. Must have 6 levels");
        }
        if (!(address.get(0).equals("") && (address.get(4).equals("")) && (address.get(5).equals("")))) {
            throw new IllegalArgumentException("We currently only support 3 level address of the type ['', 'SDG', 'SDG-n', 'cat', '', '']");
        }
        TypedQuery<IndicatorGroupHierarchy> query = persistenceManager.createNamedQuery("IGHierarchy.findByAddress234", IndicatorGroupHierarchy.class);
        query.setParameter("level2", address.get(1));
        query.setParameter("level3", address.get(2));
        query.setParameter("level4", address.get(3));
        return query.getResultList();
    }

    public static List<IndicatorGroup> findGroupWithAddress(List<String> address) {
        List<IndicatorGroupHierarchy> hierarchies = findGroupHierarchiesWithAddress(address);
        return hierarchies.stream().map(IndicatorGroupHierarchy::getLevel4).collect(Collectors.toList());
    }

    public static List<IndicatorGroup> findGroupsByArbitraryHierarchy(String ... groups) {
        if (groups.length == 0) {
            throw new IllegalArgumentException("Need at least one group name");
        }
        if (groups.length > IndicatorGroupHierarchy.MAX_LEVELS) {
            throw new IllegalArgumentException("Supplied more levels than possible");
        }
        if (groups.length == 2) {
            return findGroupWithAddress(Arrays.asList("", "SDG", groups[0], groups[1], "", ""));
        } else {
            return findGroupWithAddress(Arrays.asList(groups));
        }
    }

    public static void importIndicatorsWithGroup(Table importData) {
        // TODO: Implement later
    }

    public static void importSimpleIndicatorGroups(Table importData) {
        List<List<String>> groupings = importData.getTable();
        groupings.remove(0); // which is the header row
        // we will assume that the columns are "group", "subgroup" and "indicator"
        for (List<String> row : groupings) {
            String groupName = row.get(0);
            String subGroupName = row.get(1);
            String indicatorName = row.get(2);
            Indicator indicator = IndicatorManager.findByName(indicatorName).get(0);
            indicator.setGroup(groupName);
            indicator.setSubGroup(subGroupName);
            persistenceManager.persist(indicator);
        }
    }
}
