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

package org.metastringfoundation.healthheatmap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.grizzly.http.server.HttpServer;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metastringfoundation.healthheatmap.web.Server;

import static org.junit.Assert.assertEquals;

public class DimensionResourceTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        ResourceConfig resourceConfig = Server.createApp();
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
        String responseMsg = target.path("indicator").request().get(String.class);
        assertEquals("indicator", responseMsg);
    }
}
