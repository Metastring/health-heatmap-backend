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

package org.metastringfoundation.healthheatmap.entities;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Embeddable
public class Settlement {
    public enum SettlementType {
        ANY,
        RURAL,
        URBAN
    }

    @Enumerated(EnumType.STRING)
    private SettlementType settlement;

    public static SettlementType getSettlementTypeFromString(String settlementType) throws IllegalArgumentException {
        List<String> anyList = new ArrayList<>(Arrays.asList("any", "total", "all"));
        if (anyList.contains(settlementType.toLowerCase())) return SettlementType.ANY;

        List<String> ruralList = new ArrayList<>(Arrays.asList("rural", "r"));
        if (ruralList.contains(settlementType.toLowerCase())) return SettlementType.RURAL;

        List<String> urbanList = new ArrayList<>(Arrays.asList("urban", "u"));
        if (urbanList.contains(settlementType.toLowerCase())) return SettlementType.URBAN;

        throw new IllegalArgumentException("Unknown settlement type: " + settlementType);
    }

    public SettlementType getSettlement() {
        return settlement;
    }

    public void setSettlement(SettlementType settlement) {
        this.settlement = settlement;
    }

    @Override
    public String toString() {
        return "Settlement{" +
                "settlement=" + settlement +
                '}';
    }
}
