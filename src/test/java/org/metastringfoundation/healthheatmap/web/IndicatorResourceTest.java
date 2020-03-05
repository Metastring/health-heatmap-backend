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

package org.metastringfoundation.healthheatmap.web;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metastringfoundation.healthheatmap.logic.MockApplication;
import org.metastringfoundation.healthheatmap.logic.Application;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndicatorResourceTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        boolean injectDependencies = false;
        ResourceConfig resourceConfig = new ApplicationConfig(injectDependencies);

        Application mockApplication = new MockApplication();

        resourceConfig.registerInstances(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mockApplication).to(Application.class);
            }
        });

        server = Server.getServer(resourceConfig);
        server.start();

        Client c = ClientBuilder.newClient();

        target = c.target(Server.BASE_URI);
    }

    @After
    public void tearDown() {
        server.shutdown();
    }

    @Test
    public void testIndicator() {
        Response response = target.path("indicators").request().build("GET").invoke();
        assertEquals(200, response.getStatus());
        assertEquals("indicators", response.readEntity(String.class));
    }
}
