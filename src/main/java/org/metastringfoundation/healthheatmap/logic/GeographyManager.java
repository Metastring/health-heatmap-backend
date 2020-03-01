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
import org.metastringfoundation.healthheatmap.pojo.Geography;

import java.util.List;

public class GeographyManager {
    public static GeographyManager geographyManager;

    public static GeographyManager getInstance() {
        if (geographyManager != null) {
            return geographyManager;
        }
        geographyManager = new GeographyManager();
        return geographyManager;
    }

    private GeographyManager() {

    }

    private List<Geography> geographyList;

    public List<Geography> getGeographies() {
        return geographyList;
    }

    public void setGeographies(List<Geography> geographyList) {
        this.geographyList = geographyList;
    }

    public List<Geography> findGeographyByName(String name) {
        return null;
    }

    public Geography findGeographyFromUnmatchedGeography(UnmatchedGeography geography) throws UnknownEntityError, AmbiguousEntityError {
        String state = geography.getState();
        List<Geography> stateGeographyList = findGeographyByName(state);
        if (stateGeographyList.size() > 1) {
            throw new AmbiguousEntityError("More than one state by the name " + state + ". Please pass more specificiers");
        }
        if (stateGeographyList.size() < 1) {
            throw new UnknownEntityError("No such state known: " + state);
        }
        Geography stateGeography = stateGeographyList.get(0);
        return null;
    }
}
