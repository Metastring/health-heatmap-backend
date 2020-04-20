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

package org.metastringfoundation.healthheatmap.logic.managers;

import org.metastringfoundation.healthheatmap.dataset.entities.UnmatchedSource;
import org.metastringfoundation.healthheatmap.entities.Source;
import org.metastringfoundation.healthheatmap.logic.errors.AmbiguousEntityError;
import org.metastringfoundation.healthheatmap.storage.HibernateManager;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

public class SourceManager extends DimensionManager {
    private static List<Source> findByName(String name, EntityManager entityManager) {
        return HibernateManager.namedQueryList(
                Source.class,
                "Source.findByName",
                Collections.singletonMap("name", name),
                entityManager
        );
    }

    public static Source addSource(String name, EntityManager entityManager) {
        Source source = new Source();
        source.setName(name);
        entityManager.persist(source);
        return source;
    }

    private static List<Source> findSourceByNameCreatingIfNotExists(String name, EntityManager entityManager) {
        List<Source> sources = findByName(name, entityManager);
        if (sources.size() == 0) {
            Source source = addSource(name, entityManager);
            sources.add(source);
        }
        return sources;
    }


    public static Source findSourceFromUnmatchedSource(UnmatchedSource source, EntityManager entityManager) throws AmbiguousEntityError {
        String name = source.getName();
        List<Source> sourceList = findByName(name, entityManager);

        if (sourceList.size() > 1) {
            throw new AmbiguousEntityError("More than one indicator found by name " + name);
        }

        if (sourceList.size() == 0) {
                Source createdSource = addSource(name, entityManager);
                sourceList.add(createdSource);
        }

        return sourceList.get(0);
    }
}

