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

package org.metastringfoundation.healthheatmap.dataset;

public class UnmatchedDataElement {
    private UnmatchedGeography geography;
    private UnmatchedIndicator indicator;
    private UnmatchedSettlement settlement;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
