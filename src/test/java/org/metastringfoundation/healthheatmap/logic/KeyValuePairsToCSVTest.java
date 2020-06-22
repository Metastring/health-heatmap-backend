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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyValuePairsToCSVTest {
    @Test
    public void ensureGoodInputProducesGoodOutputCSV() throws IOException {
        List<Map<String, String>> input = List.of(
                Map.of("key1", "value1", "key2", "value2"),
                Map.of("key1", "value3", "key2", "value4")
        );
        String expected = "key1,key2\nvalue1,value2\nvalue3,value4\n";
        KeyValuePairsToCSV converter = new KeyValuePairsToCSV(input, List.of("key1", "key2"));
        String actual = converter.getCSV();
        assertEquals(expected, actual);
    }
}
