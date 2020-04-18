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

package org.metastringfoundation.healthheatmap.logic.workers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metastringfoundation.healthheatmap.entities.DataElement;

import java.util.List;

public class Aggregator {
    private static final Logger LOG = LogManager.getLogger(Aggregator.class);

    public static Float getSum(List<DataElement> input) {
        float sum = (float) 0;
        for (DataElement data : input) {
            String value = data.getValue();
            if (value != null) {
                try {
                    float parsed = Float.parseFloat(value);
                    sum = sum + parsed;
                } catch (NumberFormatException e) {
                    LOG.error("Parsed a non-float while aggreggating.", e);
                }
            }
        }
        return sum;
    }

    public static Float getAverage(List<DataElement> input) {
        float sum = getSum(input);
        Long length = Long.valueOf(input.size());
        if (length > 0) {
            return sum / length;
        } else {
            return 0f;
        }
    }
}
