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
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.metastringfoundation.healthheatmap.storage.bridges.LongStringBridge;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@NamedQueries({
        @NamedQuery(name = "Geography.findAll",
                query = "SELECT g FROM Geography g"),
        @NamedQuery(name = "Geography.findByType",
                query = "SELECT g FROM Geography g WHERE g.type = :type"),
        @NamedQuery(name = "Geography.findByName",
                query = "SELECT g FROM Geography g WHERE g.canonicalName = :name"),
        @NamedQuery(name = "Geography.findChild",
                query = "SELECT g FROM Geography g WHERE g.canonicalName = :name AND g.belongsTo = :parent")
})
@Table(name = "geographies")
public class Geography {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "geographies_sequence")
    @KeywordField(
            valueBridge = @ValueBridgeRef(type = LongStringBridge.class),
            aggregable = Aggregable.YES
    )
    private Long id;

    @Column(name = "short_code")
    @KeywordField
    private String uniqueShortCode;

    @Column(name = "wikidata_code")
    private String wikidataCode;

    @Column(name = "name")
    @FullTextField(analyzer = "english")
    private String canonicalName;

    private LocalDate established;

    private LocalDate ceased;

    @Enumerated(EnumType.STRING)
    private GeographyType type;

    @ManyToOne
    @JoinColumn(name = "belongs_to",
            foreignKey = @ForeignKey(name = "geography_belongs_to_fk")
    )
    private Geography belongsTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueShortCode() {
        return uniqueShortCode;
    }

    public void setUniqueShortCode(String uniqueShortCode) {
        this.uniqueShortCode = uniqueShortCode;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public LocalDate getEstablished() {
        return established;
    }

    public void setEstablished(LocalDate established) {
        this.established = established;
    }

    public LocalDate getCeased() {
        return ceased;
    }

    public void setCeased(LocalDate ceased) {
        this.ceased = ceased;
    }

    public GeographyType getType() {
        return type;
    }

    public void setType(GeographyType type) {
        this.type = type;
    }

    public String getWikidataCode() {
        return wikidataCode;
    }

    public void setWikidataCode(String wikidataCode) {
        this.wikidataCode = wikidataCode;
    }

    public Geography getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(Geography belongsTo) {
        this.belongsTo = belongsTo;
    }

    public enum GeographyType {
        COUNTRY,
        STATE,
        DIVISION,
        DISTRICT
    }

    @Override
    public String toString() {
        return "Geography{" +
                "id=" + id +
                ", uniqueShortCode='" + uniqueShortCode + '\'' +
                ", wikidataCode='" + wikidataCode + '\'' +
                ", canonicalName='" + canonicalName + '\'' +
                ", established=" + established +
                ", ceased=" + ceased +
                ", type=" + type +
                ", belongsTo=" + belongsTo +
                '}';
    }
}
