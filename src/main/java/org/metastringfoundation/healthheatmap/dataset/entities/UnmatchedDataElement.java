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

import java.util.Objects;

public class UnmatchedDataElement {
    private UnmatchedGeography geography;
    private UnmatchedIndicator indicator;
    private UnmatchedSettlement settlement;
    private UnmatchedGender gender;
    private String value;

    public UnmatchedGeography getGeography() {
        return geography;
    }

    public void setGeography(UnmatchedGeography geography) {
        this.geography = geography;
    }

    public UnmatchedIndicator getIndicator() {
        return indicator;
    }

    public void setIndicator(UnmatchedIndicator indicator) {
        this.indicator = indicator;
    }

    public UnmatchedSettlement getSettlement() {
        return settlement;
    }

    public void setSettlement(UnmatchedSettlement settlement) {
        this.settlement = settlement;
    }

    public UnmatchedGender getGender() {
        return gender;
    }

    public void setGender(UnmatchedGender gender) {
        this.gender = gender;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UnmatchedDataElement other = (UnmatchedDataElement) obj;
        return (Objects.deepEquals(other.getValue(), this.getValue()) &&
                Objects.deepEquals(other.getGeography(), this.getGeography()) &&
                Objects.deepEquals(other.getIndicator(), this.getIndicator()) &&
                Objects.deepEquals(other.getSettlement(), this.getSettlement()) &&
                Objects.deepEquals(other.getGender(), this.getGender())
        );
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                "geography " + getGeography() + "\n" +
                "indicator " + getIndicator() + "\n" +
                "settlement " + getSettlement() + "\n" +
                "gender " + getGender() + "\n" +
                "value " + getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(geography, indicator, settlement, gender, value);
    }
}
