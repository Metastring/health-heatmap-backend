
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

import org.junit.jupiter.api.Test;
import org.metastringfoundation.healthheatmap.logic.Application;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class HealthResourceTest {
    @Test
    public void testHealth() {
        Application mockApp = mock(Application.class);
        HealthResource healthResource = new HealthResource(mockApp);
        Map<String, String> response = healthResource.getHealth();
        assertEquals(Map.of("database", "GREEN"), response);
    }
}
