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

package org.metastringfoundation.healthheatmap.logic.workers;

import org.metastringfoundation.healthheatmap.dataset.DatasetIntegrityError;
import org.metastringfoundation.healthheatmap.dataset.table.Table;
import org.metastringfoundation.healthheatmap.logic.DefaultApplication;
import org.metastringfoundation.healthheatmap.logic.managers.IndicatorGroupingManager;

import javax.persistence.EntityManager;

public class IndicatorGroupUploadWorker {
    private static final EntityManager persistenceManager = DefaultApplication.persistenceManager;

    public static void importIndicatorGroupSimple(Table table) throws DatasetIntegrityError {
        persistenceManager.getTransaction().begin();
        IndicatorGroupingManager.importSimpleIndicatorGroups(table);
        persistenceManager.getTransaction().commit();
    }
}
