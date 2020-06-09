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

package org.metastringfoundation.healthheatmap.logic.beanconverters;

import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiMapToDataQueryTest {

    @Test
    void normalizeParams() {
        MultivaluedMap<String, String> input = new MultivaluedHashMap<>();
        input.addAll("a", "1", "2,3", "4");
        input.addAll("b", "4", "6", "7,8");

        Map<String, Collection<String>> expected = Map.of(
                "a", List.of("1", "2", "3", "4"),
                "b", List.of("4", "6", "7", "8")
        );
        Map<String, Collection<String>> actual = MultiMapToDataQuery.normalizeParams(input);

        assertEquals(expected, actual);
    }

    @Test
    void splitsCommaSeparatedStringInsideList() {
        Collection<String> input = List.of("1,2", "3", "4,5");
        Collection<String> expected = List.of("1", "2", "3", "4", "5");
        Collection<String> actual = MultiMapToDataQuery.splitCommaSeparatedElementsIn(input);
        assertEquals(expected, actual);
    }
}
