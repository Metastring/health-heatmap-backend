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
import java.util.Objects;

@Entity
@Table(name = "geography_names")
public class GeographyName {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Geography geography;

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

    public Geography getGeography() {
        return geography;
    }

    public void setGeography(Geography geography) {
        this.geography = geography;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeographyName that = (GeographyName) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(geography, that.geography);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, geography);
    }
}
