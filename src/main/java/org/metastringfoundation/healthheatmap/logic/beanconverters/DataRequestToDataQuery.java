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

import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.web.beans.DataRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataRequestToDataQuery {
    public static org.metastringfoundation.healthheatmap.storage.beans.DataQuery convert(DataRequest dataRequest) {
        DataQuery dataQuery = new DataQuery();
        Map<String, Collection<String>> must = new HashMap<>();
        if (dataRequest.getIndicators() != null) {
            must.put("indicator", dataRequest.getIndicators());
        }
        dataQuery.setTerms(must);
        return dataQuery;
    }
}
