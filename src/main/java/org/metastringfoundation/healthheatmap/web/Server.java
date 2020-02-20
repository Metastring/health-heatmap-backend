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
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.DefaultApplication;
import org.metastringfoundation.healthheatmap.storage.Database;
import org.metastringfoundation.healthheatmap.storage.PostgreSQL;

import java.io.IOException;
import java.net.URI;

public class Server {
    private static final Logger LOG = LogManager.getLogger(Server.class);
    public static final String BASE_URI = "http://localhost:8080/myapp/";

    public static HttpServer server;

    private static void configureProductionServer() {
        ResourceConfig rc = createApp();
        rc = injectProductionDependencies(rc);
        server = getServer(rc);
    }

    private static void startServer() {
        try {
            server.start();
        } catch (IOException ex) {
            LOG.fatal(ex);
        }
        LOG.info("Application started.\nTry out {}", BASE_URI);
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            LOG.fatal(ex);
        }
    }

    public static void startProductionServer() {
        configureProductionServer();
        startServer();
    }

    public static HttpServer getServer(ResourceConfig rc) {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        return server;
    }

    public static ResourceConfig createApp() {
        final ResourceConfig rc = new ResourceConfig().packages("org.metastringfoundation.healthheatmap");
        return rc;
    }

    public static ResourceConfig injectProductionDependencies(ResourceConfig rc) {
        Database psql = new PostgreSQL();
        DefaultApplication app = new DefaultApplication();
        app.setPsql(psql);
        rc.registerInstances(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(app).to(Application.class);
                bind(psql).to(Database.class);
            }
        });
        return rc;
    }
}
