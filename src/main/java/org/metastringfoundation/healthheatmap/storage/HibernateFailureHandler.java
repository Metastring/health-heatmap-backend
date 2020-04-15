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

package org.metastringfoundation.healthheatmap.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.search.engine.reporting.EntityIndexingFailureContext;
import org.hibernate.search.engine.reporting.FailureContext;
import org.hibernate.search.engine.reporting.FailureHandler;

import java.util.ArrayList;
import java.util.List;

public class HibernateFailureHandler implements FailureHandler {
    private static final Logger LOG = LogManager.getLogger(HibernateFailureHandler.class);

    @Override
    public void handle(FailureContext context) {
        String failingOperationDescription = context.getFailingOperation().toString();
        Throwable throwable = context.getThrowable();

        LOG.fatal(failingOperationDescription);
        LOG.fatal(throwable.getMessage());
    }

    @Override
    public void handle(EntityIndexingFailureContext context) {
        String failingOperationDescription = context.getFailingOperation().toString();
        Throwable throwable = context.getThrowable();
        List<String> entityReferencesAsStrings = new ArrayList<>();
        for ( Object entityReference : context.getEntityReferences() ) {
            entityReferencesAsStrings.add( entityReference.toString() );
        }

        LOG.fatal(failingOperationDescription);
        LOG.fatal(throwable.getMessage());

        LOG.debug(entityReferencesAsStrings);
    }

}
