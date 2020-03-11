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
import org.metastringfoundation.healthheatmap.dataset.Table;

public interface Application {
    String getIndicators() throws ApplicationError;
    String getEntities() throws ApplicationError;

    String addIndicator(String indicatorName) throws ApplicationError;

    String saveEntity(String entityJSON);
    String getDimension(String dimension);

    String getHealth();

    Long saveDataset(Dataset dataset) throws ApplicationError;
    void saveTable(String name, Table table) throws ApplicationError;

    void shutDown() throws ApplicationError;
}
