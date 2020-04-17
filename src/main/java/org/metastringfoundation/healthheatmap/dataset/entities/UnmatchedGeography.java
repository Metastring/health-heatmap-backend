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

package org.metastringfoundation.healthheatmap.dataset.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.metastringfoundation.healthheatmap.helpers.PatternParsingAssistants.quotedDimension;

public class UnmatchedGeography {
    private String state;
    private String district;

    public static Map<Integer, UnmatchedGeography> getGeography(Map<Integer, Map<String, String>> dimensionsMap) {
        Map<Integer, UnmatchedGeography> result = new HashMap<>();
        for (Map.Entry<Integer, Map<String, String>> row: dimensionsMap.entrySet()) {
            String state = row.getValue().get(quotedDimension("entity.state"));
            String district = row.getValue().get(quotedDimension("entity.district"));
            if (!(state == null && district == null)) {
                UnmatchedGeography geography = new UnmatchedGeography();
                geography.setState(state);
                geography.setDistrict(district);
                result.put(row.getKey(), geography);
            }
        }
        return result;
    }

    public UnmatchedGeography() {}

    public UnmatchedGeography(String state, String district) {
        setState(state);
        setDistrict(district);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UnmatchedGeography other = (UnmatchedGeography) obj;
        return (other.getDistrict().equals(this.getDistrict())
                && other.getState().equals(this.getState())
        );
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                "state: " + getState() + "\n" +
                "district: " + getDistrict();
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, district);
    }
}
