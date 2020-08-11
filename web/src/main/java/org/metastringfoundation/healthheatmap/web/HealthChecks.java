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

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.metastringfoundation.healthheatmap.logic.Application;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
public class HealthChecks {

    @Inject
    Application app;

    @Produces
    @Liveness
    HealthCheck live() {
        return () -> HealthCheckResponse.up("Server");
    }

    @Produces
    @Readiness
    HealthCheck appInternals() {
        return () -> HealthCheckResponse.named("Application").state(checkAppHealth()).build();
    }

    private boolean checkAppHealth() {
        try {
            return app.getHealth();
        } catch (IOException e) {
            return false;
        }
    }
}
