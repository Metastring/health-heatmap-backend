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

package org.metastringfoundation.healthheatmap.web.beans;

import javax.ws.rs.QueryParam;
import java.util.List;

import static org.metastringfoundation.healthheatmap.logic.utils.StringUtils.commaSeparated;

public class DataRequest {
    private List<String> indicators;
    private List<String> geographies;
    private List<String> sources;

    @QueryParam("indicators")
    public void setIndicators(String indicators) {
        this.indicators = commaSeparated(indicators);
    }

    @QueryParam("geographies")
    public void setGeographies(String geographies) {
        this.geographies = commaSeparated(geographies);
    }

    @QueryParam("sources")
    public void setSources(String sources) {
        this.sources = commaSeparated(sources);
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public List<String> getGeographies() {
        return geographies;
    }

    public List<String> getSources() {
        return sources;
    }
}
