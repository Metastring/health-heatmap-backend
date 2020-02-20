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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Server {
    private static final Logger LOG = LogManager.getLogger(Server.class);
    public static final String BASE_URI = "http://localhost:8080/myapp/";

    public static HttpServer server;

    public static void startServer() {
        try {
            server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), createApp(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            LOG.info("Application started.\nTry out {}\nStop the application using CTRL+C", BASE_URI);

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            LOG.fatal(ex);
        }

    }

    public static ResourceConfig createApp() {
        final ResourceConfig rc = new ResourceConfig().packages("org.metastringfoundation.healthheatmap");
        return rc;
    }
}
