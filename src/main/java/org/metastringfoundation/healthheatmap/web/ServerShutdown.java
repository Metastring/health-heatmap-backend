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
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Based on <a href="https://stackoverflow.com/questions/44572276/what-is-the-proper-way-to-gracefully-shutdown-a-grizzly-server-embedded-with-j">Bentaye's stackoverflow post</a>
 */
public class ServerShutdown extends Thread {
    public static final String THREAD_NAME = "Grizzly Server Shutdown Hook";

    public static final int GRACE_PERIOD = 60;
    public static final TimeUnit GRACE_PERIOD_TIME_UNIT = TimeUnit.SECONDS;

    private static final Logger LOG = LogManager.getLogger(ServerShutdown.class);

    private final HttpServer server;

    /**
     * @param server The server to shut down
     */
    public ServerShutdown(HttpServer server) {
        this.server = server;
        setName(THREAD_NAME);
    }

    @Override
    public void run() {
        LOG.info("Running Grizzly Server Shutdown Hook.");
        LOG.info("Shutting down server.");
        GrizzlyFuture<HttpServer> future = server.shutdown(GRACE_PERIOD, GRACE_PERIOD_TIME_UNIT);

        try {
            LOG.info("Waiting for server to shut down... Grace period is " + GRACE_PERIOD + " " + GRACE_PERIOD_TIME_UNIT);
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error while shutting down server.", e);
        }

        LOG.info("Server stopped.");
    }
}
