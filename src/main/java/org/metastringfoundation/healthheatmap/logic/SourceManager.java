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

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class SourceManager {
    private static SourceManager sourceManager;

    public static SourceManager getInstance() {
        if (sourceManager != null) {
            return sourceManager;
        }
        sourceManager = new SourceManager();
        return sourceManager;
    }

    private EntityManager persistenceManager;

    public void setPersistenceManager(EntityManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public List<Source> getAllSources() {
        TypedQuery<Source> query = persistenceManager.createNamedQuery("Source.findAll", Source.class);
        return query.getResultList();
    }

    public Source addSourceWithCommit(String name) {
        persistenceManager.getTransaction().begin();
        Source source = addSource(name);
        persistenceManager.getTransaction().commit();
        return source;
    }

    public Source addSource(String name) {
        Source source = new Source();
        source.setName(name);
        persistenceManager.persist(source);
        return source;
    }

    private List<Source> findByName(String name) {
        TypedQuery<Source> query = persistenceManager.createNamedQuery("Source.findByName", Source.class);
        query.setParameter("name", name);
        return query.getResultList();
    }

    private List<Source> findSourceByNameCreatingIfNotExists(String name) {
        List<Source> sources = findByName(name);
        if (sources.size() == 0) {
            Source source = addSource(name);
            sources.add(source);
        }
        return sources;
    }

    public Source findSourceFromUnmatchedSource(UnmatchedSource source) throws AmbiguousEntityError {
        String name = source.getName();
        List<Source> sourceList = findSourceByNameCreatingIfNotExists(name);

        if (sourceList.size() > 1) {
            throw new AmbiguousEntityError("More than one source found by name " + name);
        }
        return sourceList.get(0);
    }
}
