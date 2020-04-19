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

import org.metastringfoundation.healthheatmap.dataset.Dataset;
import org.metastringfoundation.healthheatmap.dataset.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.dataset.table.Table;
import org.metastringfoundation.healthheatmap.entities.Geography;
import org.metastringfoundation.healthheatmap.entities.Indicator;
import org.metastringfoundation.healthheatmap.logic.errors.ApplicationError;
import org.metastringfoundation.healthheatmap.web.ResponseTypes.AggregatedData;

import java.util.List;

public interface Application {
    List<Indicator> getIndicators();

    List<Geography> getEntities(String type);

    Indicator addIndicator(String indicatorName);

    String getHealth();

    Long saveDataset(Dataset dataset) throws ApplicationError;

    void saveTable(String name, Table table) throws ApplicationError;

    void importIndicatorGrouping(Table table) throws DatasetIntegrityError;

    void shutDown();

    AggregatedData getData(String indicatorGroups, String indicatorSubGroups, String indicators, String geographies, String geographyTypes, String sources, String aggregation);

    void exportIndicators(String path);
}
