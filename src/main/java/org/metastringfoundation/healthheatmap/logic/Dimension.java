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

import javax.json.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Dimension {
    String getName();
    void setName(String name);

    String getShortCode();
    void setShortCode(String shortCode);

    String getDescription();
    void setDescription(String description);

    Optional<Collection<JsonObject>> getCanonicalValues();
    Optional<Collection<JsonObject>> getRawValues();
    DimensionType getType();
    List<JsonObject> validateValues(List<JsonObject> values);
    default JsonObject validateValue(JsonObject value) {
        return validateValues(List.of(value)).get(0);
    }
}
