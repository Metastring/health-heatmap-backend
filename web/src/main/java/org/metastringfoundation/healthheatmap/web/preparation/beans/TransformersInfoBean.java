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

package org.metastringfoundation.healthheatmap.web.preparation.beans;

import java.util.List;
import java.util.Map;

public class TransformersInfoBean {
    public Map<Map<String, String>, List<Map<String, String>>> rules;
    public List<Map<String, String>> failures;

    public TransformersInfoBean(Map<Map<String, String>, List<Map<String, String>>> rules, List<Map<String, String>> failures) {
        this.rules = rules;
        this.failures = failures;
    }
}
