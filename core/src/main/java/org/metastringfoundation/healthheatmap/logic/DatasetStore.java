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

import org.metastringfoundation.healthheatmap.beans.Filter;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;
import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DatasetStore {
    void save(HealthDataset dataset) throws IOException;

    void shutdown() throws IOException;

    DataQueryResult query(DataQuery dataQuery) throws IOException;

    List<Map<String, Object>> getAllTermsOfFields(FilterAndSelectFields filterAndFields) throws IOException;

    void factoryReset() throws IOException;

    boolean getHealth() throws IOException;

    Map<String, List<String>> getDimensionsPossibleAt(List<String> dimensions, Filter filter) throws IOException;
}
