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

import org.metastringfoundation.healthheatmap.dataset.UnmatchedGeography;
import org.metastringfoundation.healthheatmap.pojo.Entity;

import java.util.List;

public class EntityManager {
    public static EntityManager entityManager;

    public static EntityManager getInstance() {
        if (entityManager != null) {
            return entityManager;
        }
        entityManager = new EntityManager();
        return entityManager;
    }

    private EntityManager() {

    }

    private List<Entity> entityList;

    public List<Entity> getEntities() {
        return entityList;
    }

    public void setEntities(List<Entity> entityList) {
        this.entityList = entityList;
    }

    public List<Entity> queryEntities(

    ) {
        return null;
    }

    public List<Entity> findEntityByName(String name) {

    }

    public Entity findEntityFromGeography(UnmatchedGeography geography) throws UnknownEntityError, AmbiguousEntityError {
        String state = geography.getState();
        List<Entity> stateEntityList = findEntityByName(state);
        if (stateEntityList.size() > 1) {
            throw new AmbiguousEntityError("More than one state by the name " + state + ". Please pass more specificiers");
        }
        if (stateEntityList.size() < 1) {
            throw new UnknownEntityError("No such state known: " + state);
        }
        Entity stateEntity = stateEntityList.get(0);
    }
}
