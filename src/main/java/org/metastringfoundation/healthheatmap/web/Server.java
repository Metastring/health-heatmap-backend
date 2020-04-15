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
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.metastringfoundation.healthheatmap.Main;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

public class Server {
    private static final Logger LOG = LogManager.getLogger(Server.class);
    public static final String BASE_URI = "http://localhost:8080/data-reader/api/";

    public static HttpServer server;

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
        ResourceConfig productionApp = new ApplicationConfig();
        server = getServer(productionApp);
        startServer();
    }

    public static HttpServer getServer(ResourceConfig rc) {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        try {
            final Properties properties = new Properties();
            properties.load(Server.class.getClassLoader().getResourceAsStream("project.properties"));
            String staticDir = "target/" + properties.getProperty("artifactId") + "-" + properties.getProperty("version");
            String staticRoute = "/data-reader";
            LOG.info("Serving static contents from " + staticDir + " at " + staticRoute);
            server.getServerConfiguration().addHttpHandler(new StaticHttpHandler(staticDir), staticRoute);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return server;
    }
}
