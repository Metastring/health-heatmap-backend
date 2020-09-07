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

package org.metastringfoundation.healthheatmap.beanconverters;

import org.metastringfoundation.healthheatmap.beans.DataResponse;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import java.util.List;
import java.util.Map;

public class DataQueryResultToDataResponse {
    public static DataResponse convert(DataQueryResult queryResult, List<String> mustInclude) {
        if (mustInclude == null) {
            mustInclude = List.of();
        }
        DataResponse dataResponse = new DataResponse();
        List<Map<String, String>> result = queryResult.getResult();
        filter(result, mustInclude);
        dataResponse.setData(result);
        return dataResponse;
    }

    private static void filter(List<Map<String, String>> result, List<String> mustInclude) {
        result.forEach(m -> m.entrySet().removeIf(e -> unnecessary(e.getKey(), mustInclude)));
    }

    private static boolean unnecessary(String k, List<String> mustInclude) {
        for (String include : mustInclude) {
            if (k.startsWith(include)) {
                return true;
            }
        }
        return k.startsWith("meta.original") || k.startsWith("meta.transformed");
    }
}
