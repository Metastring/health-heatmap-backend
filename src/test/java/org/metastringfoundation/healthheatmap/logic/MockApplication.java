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
import org.metastringfoundation.healthheatmap.logic.errors.ApplicationError;
import org.metastringfoundation.healthheatmap.web.ResponseTypes.AggregatedData;

import java.util.List;

public class MockApplication implements Application {
    @Override
    public String getIndicators() throws ApplicationError {
        return "indicators";
    }

    @Override
    public List<Geography> getEntities(String type) {
        return null;
    }

    @Override
    public String addIndicator(String indicatorName) throws ApplicationError {
        return null;
    }

    @Override
    public String saveEntity(String entityJSON) {
        return null;
    }

    @Override
    public String getDimension(String dimension) {
        return null;
    }

    @Override
    public String getHealth() {
        return null;
    }

    @Override
    public Long saveDataset(Dataset dataset) {
        return null;
    }

    @Override
    public void saveTable(String name, Table table) throws ApplicationError {

    }

    @Override
    public void importIndicatorGrouping(Table table) throws DatasetIntegrityError {

    }

    @Override
    public void shutDown() {

    }

    @Override
    public AggregatedData getData(String indicatorGroups, String indicatorSubGroups, String indicators, String geographies, String geographyTypes, String sources, String aggregation) {
        return null;
    }

    @Override
    public void exportIndicators(String path) {

    }
}
