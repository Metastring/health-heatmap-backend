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

import javax.persistence.Entity;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Entity
public class Geography {
    public enum GeographyType {
        DISTRICT,
        STATE
    }

    private String canonicalName;
    private Map<Locale, String> translations;
    private List<Geography> belongsTo;
    private GeographyType type;
    private Date established;
    private Date ceased;

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

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
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

    public GeographyType getType() {
        return type;
    }

    public void setType(GeographyType type) {
        this.type = type;
    }
}
