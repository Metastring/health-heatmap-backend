
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

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test; // JerseyTest doesn't work with Junit5, https://github.com/eclipse-ee4j/jersey/issues/3662

import static org.junit.Assert.assertEquals;

public class HealthResourceTest extends JerseyTest {
    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(Health.class);
    }

    @Test
    public void testRequestCounter() throws InterruptedException {
        String response = target().path("/health").request().get(String.class);
        assertEquals("healthyenough", response);
    }
}
