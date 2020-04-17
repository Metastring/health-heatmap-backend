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

import javax.persistence.*;

@Entity
@Table(name = "indicator_group_hierarchy")
public class IndicatorGroupHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indicator_group_hierarchy_sequence")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "level_1",
            foreignKey = @ForeignKey(name = "indicator_group_level_1_fk")
    )
    private IndicatorGroup level1;

    @ManyToOne
    @JoinColumn(name = "level_2",
            foreignKey = @ForeignKey(name = "indicator_group_level_2_fk")
    )
    private IndicatorGroup level2;

    @ManyToOne
    @JoinColumn(name = "level_3",
            foreignKey = @ForeignKey(name = "indicator_group_level_3_fk")
    )
    private IndicatorGroup level3;

    @ManyToOne
    @JoinColumn(name = "level_4",
            foreignKey = @ForeignKey(name = "indicator_group_level_4_fk")
    )
    private IndicatorGroup level4;

    @ManyToOne
    @JoinColumn(name = "level_5",
            foreignKey = @ForeignKey(name = "indicator_group_level_5_fk")
    )
    private IndicatorGroup level5;

    @ManyToOne
    @JoinColumn(name = "level_6",
            foreignKey = @ForeignKey(name = "indicator_group_level_6_fk")
    )
    private IndicatorGroup level6;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IndicatorGroup getLevel1() {
        return level1;
    }

    public void setLevel1(IndicatorGroup level1) {
        this.level1 = level1;
    }

    public IndicatorGroup getLevel2() {
        return level2;
    }

    public void setLevel2(IndicatorGroup level2) {
        this.level2 = level2;
    }

    public IndicatorGroup getLevel3() {
        return level3;
    }

    public void setLevel3(IndicatorGroup level3) {
        this.level3 = level3;
    }

    public IndicatorGroup getLevel4() {
        return level4;
    }

    public void setLevel4(IndicatorGroup level4) {
        this.level4 = level4;
    }

    public IndicatorGroup getLevel5() {
        return level5;
    }

    public void setLevel5(IndicatorGroup level5) {
        this.level5 = level5;
    }

    public IndicatorGroup getLevel6() {
        return level6;
    }

    public void setLevel6(IndicatorGroup level6) {
        this.level6 = level6;
    }
}
