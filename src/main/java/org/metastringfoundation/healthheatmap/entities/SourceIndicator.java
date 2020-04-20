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

@Table(name = "source_indicators")
public class SourceIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_indicators_sequence")
    private Long id;

    @Column(name = "name")
    @FullTextField(analyzer = "english")
    private String name;

    @JoinColumn(name = "source",
            foreignKey =  @ForeignKey(name = "source_indicator_source_fk")
    )
    private Source source;

    @JoinColumn(name = "original",
            foreignKey = @ForeignKey(name = "source_indicator_original_fk")
    )
    private Indicator original;

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

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Indicator getOriginal() {
        return original;
    }

    public void setOriginal(Indicator original) {
        this.original = original;
    }
}
