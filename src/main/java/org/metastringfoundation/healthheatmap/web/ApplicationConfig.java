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

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.logic.DefaultApplication;

public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        boolean production = true;
        setup(production);
    }

    private void setup(boolean production) {
        packages("org.metastringfoundation.healthheatmap,io.swagger.v3.jaxrs2.integration.resources");
        if (production) {
            DefaultApplication app = new DefaultApplication();
            registerInstances(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(app).to(Application.class);
                }
            });
        }
    }

    public ApplicationConfig(boolean production) {
        setup(production);
    }
}
