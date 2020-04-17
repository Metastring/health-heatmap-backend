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
import java.util.List;

@Entity
@Table(name = "indicator_groups")
public class IndicatorGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indicator_groups_sequence")
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "indicator_group_mappings",
            joinColumns = @JoinColumn(name = "indicator_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<Indicator> indicators;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<Indicator> indicators) {
        this.indicators = indicators;
    }
}
