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

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import javax.persistence.*;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name = "Indicator.findById",
                query = "SELECT i FROM Indicator i WHERE i.id = :id"),
        @NamedQuery(name = "Indicator.findAll",
                query = "SELECT i FROM Indicator i"),
        @NamedQuery(name = "Indicator.findByName",
                query = "SELECT i FROM Indicator i WHERE i.canonicalName = :name")
})
@Table(name = "indicators")
public class Indicator {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indicators_sequence")
    private Long id;

    @Column(name = "short_code")
    @FullTextField(analyzer = "english")
    private String shortCode;

    @Column(name = "name")
    @FullTextField(analyzer = "english")
    private String canonicalName;

    private String derivation;

    @ManyToMany(mappedBy = "indicators")
    private List<IndicatorGroup> groups;

    @Column(name = "group_1")
    private String group;

    @Column(name = "group_2")
    private String subGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String getDerivation() {
        return derivation;
    }

    public void setDerivation(String derivation) {
        this.derivation = derivation;
    }

    public List<IndicatorGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<IndicatorGroup> groups) {
        this.groups = groups;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }
}