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

import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.storage.DatasetStore;
import org.metastringfoundation.healthheatmap.storage.ElasticManager;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ApplicationDefaultTest {
    private final DatasetStore mockDatasetStore = mock(ElasticManager.class);
    private final Application application = new ApplicationDefault(mockDatasetStore);

    @Test
    public void applicationCorrectlyRoutesDataQueryToDatasetStore() throws IOException {
        DataQuery dataQuery = mock(DataQuery.class);
        DataQueryResult stubResult = mock(DataQueryResult.class);
        when(mockDatasetStore.query(dataQuery)).thenReturn(stubResult);
        DataQueryResult actual = application.query(dataQuery);
        verify(mockDatasetStore, times(1)).query(dataQuery);
        assertEquals(actual, stubResult);
    }

}
