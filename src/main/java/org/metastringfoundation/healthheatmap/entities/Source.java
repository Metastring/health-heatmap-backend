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

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.metastringfoundation.healthheatmap.storage.bridges.LongStringBridge;

import javax.persistence.*;

@Entity
@Table(name = "sources")
@NamedQueries({
        @NamedQuery(name = "Source.findAll",
                query = "SELECT s FROM Source s"),
        @NamedQuery(name = "Source.findByName",
                query = "SELECT s FROM Source s WHERE s.name = :name")
})
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sources_sequence")
    @KeywordField(
            valueBridge = @ValueBridgeRef(type = LongStringBridge.class),
            aggregable = Aggregable.YES
    )
    private Long id;

    @KeywordField
    private String name;

    @Embedded
    private TimePeriod timePeriod;

    @ManyToOne
    @JoinColumn(name = "belongs_to",
            foreignKey = @ForeignKey(name = "source_belongs_to_fk")
    )
    private Source belongsTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Source getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(Source belongsTo) {
        this.belongsTo = belongsTo;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }
}
