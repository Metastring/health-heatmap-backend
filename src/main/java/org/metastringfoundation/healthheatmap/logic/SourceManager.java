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

package org.metastringfoundation.healthheatmap.logic;

import org.metastringfoundation.healthheatmap.dataset.UnmatchedSource;
import org.metastringfoundation.healthheatmap.pojo.Source;

import javax.persistence.TypedQuery;
import java.util.List;

public class SourceManager extends DimensionManager {
    private static List<Source> findByName(String name) {
        TypedQuery<Source> query = persistenceManager.createNamedQuery("Source.findByName", Source.class);
        query.setParameter("name", name);
        return query.getResultList();
    }

    public static Source addSource(String name) {
        Source source = new Source();
        source.setName(name);
        persistenceManager.persist(source);
        return source;
    }

    private static List<Source> findSourceByNameCreatingIfNotExists(String name) {
        List<Source> sources = findByName(name);
        if (sources.size() == 0) {
            Source source = addSource(name);
            sources.add(source);
        }
        return sources;
    }


    public static Source findSourceFromUnmatchedSource(UnmatchedSource source) throws AmbiguousEntityError {
        String name = source.getName();
        List<Source> sourceList = findByName(name);

        if (sourceList.size() > 1) {
            throw new AmbiguousEntityError("More than one indicator found by name " + name);
        }

        if (sourceList.size() == 0) {
                Source createdSource = addSource(name);
                sourceList.add(createdSource);
        }

        return sourceList.get(0);
    }
}

