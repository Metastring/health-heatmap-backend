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
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

public class Server {
    private static final Logger LOG = LogManager.getLogger(Server.class);

    private final String DEPLOYMENT_URI = "http://localhost:8080/";
    private final String BASE_URI = DEPLOYMENT_URI + "api/";

    private final ResourceConfig resourceConfig = new ResourceConfig();
    private final HttpServer server;

    private Server(String env) throws IOException {
        if ("production".equals(env)) {
            configureAsProductionServer();
        } else {
            configureAsDevelopmentServer();
        }
        server = getServer(resourceConfig);
        addDocHandler(server);
        addGracefulShutdownHook();
    }

    public static void start(String env) throws IOException {
        Server server = new Server(env);
        server.startServer();
    }

    private void configureAsProductionServer() {
        configureClasses();
    }

    private void configureAsDevelopmentServer() {
        configureClasses();
        resourceConfig.register(new DevelopmentExceptionMapper());
    }

    private void configureClasses() {
        resourceConfig.packages(
                "org.metastringfoundation.healthheatmap;" +
                        "io.swagger.v3.jaxrs2.integration.resources"
        );
    }

    private void startServer() throws IOException {
        server.start();
        LOG.info("Application started.\nTry out {}", DEPLOYMENT_URI);
    }

    private HttpServer getServer(ResourceConfig rc) {
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
    }

    private void addDocHandler(HttpServer server) throws IOException {
        final Properties properties = new Properties();
        properties.load(Server.class.getClassLoader().getResourceAsStream("project.properties"));
        String staticDir = "target/" + properties.getProperty("artifactId") + "-" + properties.getProperty("version");
        String staticRoute = "/doc";
        LOG.info("Serving static contents from " + staticDir + " at " + staticRoute);
        server.getServerConfiguration().addHttpHandler(new StaticHttpHandler(staticDir), staticRoute);
    }

    private void addGracefulShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new ServerShutdown(server));
    }
}
