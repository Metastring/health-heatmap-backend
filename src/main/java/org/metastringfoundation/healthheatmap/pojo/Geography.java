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

package org.metastringfoundation.healthheatmap.pojo;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Entity
@Table(name = "geographies")
public class Geography {
    public enum GeographyType {
        DISTRICT,
        STATE
    }


    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @Column(name = "short_code")
    private String uniqueShortCode;

    @Column(name = "name")
    private String canonicalName;

    @Column(name = "established")
    private Date established;

    @Column(name = "ceased")
    private Date ceased;

    @Enumerated(EnumType.STRING)
    private GeographyType type;

    @ManyToOne
    private Geography belongsTo;

    @Transient
    private Map<Locale, String> translations;

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

    public Date getEstablished() {
        return established;
    }

    public void setEstablished(Date established) {
        this.established = established;
    }

    public Date getCeased() {
        return ceased;
    }

    public void setCeased(Date ceased) {
        this.ceased = ceased;
    }

    public GeographyType getType() {
        return type;
    }

    public void setType(GeographyType type) {
        this.type = type;
    }

    public Map<Locale, String> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<Locale, String> translations) {
        this.translations = translations;
    }

    public List<Geography> getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(List<Geography> belongsTo) {
        this.belongsTo = belongsTo;
    }
}
