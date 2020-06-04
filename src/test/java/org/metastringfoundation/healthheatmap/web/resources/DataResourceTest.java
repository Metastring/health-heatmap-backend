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

package org.metastringfoundation.healthheatmap.web.resources;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;
import org.metastringfoundation.healthheatmap.web.beans.DataResponse;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataResourceTest extends JerseyTest {
    private final Application mockApplication = mock(Application.class);
    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(DataResource.class).registerInstances(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mockApplication).to(Application.class);
            }
        });
    }

    @Test
    public void testQueryCorrectlyCallsApplication() throws IOException {
        DataQueryResult dataQueryResult = mock(DataQueryResult.class);
        when(mockApplication.query(any(DataQuery.class))).thenReturn(dataQueryResult);
        DataResponse actual = target().path("/data").queryParam("indicators", List.of("indicator1")).request().get(DataResponse.class);
        assertEquals("healthyenough", dataQueryResult);
    }
}
