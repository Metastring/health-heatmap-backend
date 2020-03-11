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

package org.metastringfoundation.healthheatmap.helpers;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListUtilsTest {

    @Test
    void transposeEqualOnes() {
        List<List<Integer>> inputList = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6),
                Arrays.asList(7, 8, 9)
        );

        List<List<Integer>> expectedTranspose = Arrays.asList(
                Arrays.asList(1, 4, 7),
                Arrays.asList(2, 5, 8),
                Arrays.asList(3, 6, 9)
        );

        assertEquals(expectedTranspose, ListUtils.transpose(inputList));
    }

    @Test
    void transposeUnequalOnes() {
        List<List<Integer>> inputList = Arrays.asList(
                Arrays.asList(1, 2, 3, 4),
                Arrays.asList(4, 5),
                Arrays.asList(7, 8, 9)
        );

        List<List<Integer>> expectedTranspose = Arrays.asList(
                Arrays.asList(1, 4, 7),
                Arrays.asList(2, 5, 8),
                Arrays.asList(3, 9),
                Arrays.asList(4)
        );

        assertEquals(expectedTranspose, ListUtils.transpose(inputList));
    }
}