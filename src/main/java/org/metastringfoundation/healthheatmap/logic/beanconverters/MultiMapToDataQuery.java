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

package org.metastringfoundation.healthheatmap.logic.beanconverters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiMapToDataQuery {
    private static final Logger LOG = LogManager.getLogger(MultiMapToDataQuery.class);

    public static DataQuery convert(MultivaluedMap<String, String> dataRequest) {
        DataQuery dataQuery = new DataQuery();
        Map<String, Collection<String>> must = normalizeParams(dataRequest);
        LOG.debug(dataRequest + " normalized to " + must);
        dataQuery.setMust(must);
        return dataQuery;
    }

    public static Map<String, Collection<String>> normalizeParams(MultivaluedMap<String, String> input) {
        return input.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        mapEntry -> splitCommaSeparatedElementsIn(mapEntry.getValue())
                ));
    }

    public static Collection<String> splitCommaSeparatedElementsIn(Collection<String> input) {
        return input.stream()
                .map(e -> e.split(","))
                .map(List::of)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
