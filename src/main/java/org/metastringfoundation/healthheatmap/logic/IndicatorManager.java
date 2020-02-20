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

import org.metastringfoundation.healthheatmap.pojo.Indicator;

import java.util.ArrayList;
import java.util.List;

public class IndicatorManager {
    private List<Indicator> indicatorList;

    public IndicatorManager() {
        loadIndicators();
    }

    public void loadIndicators() {
        indicatorList = new ArrayList<>();

        Indicator a = new Indicator();
        a.setId("TEST");
        a.setCanonicalName("Test Indicator");
        indicatorList.add(a);

        Indicator b = new Indicator();
        b.setId("MMR");
        b.setCanonicalName("Maternal Mortality Rate");
        indicatorList.add(b);
    }

    public List<Indicator> getIndicators () {
        return indicatorList;
    }
}
