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

package org.metastringfoundation.healthheatmap.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class IndicatorGroup {
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<IndicatorGroup> subGroups;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Indicator> indicators;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IndicatorGroup> getSubGroups() {
        return subGroups;
    }

    public void setSubGroups(List<IndicatorGroup> subGroups) {
        this.subGroups = subGroups;
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<Indicator> indicators) {
        this.indicators = indicators;
    }
}
