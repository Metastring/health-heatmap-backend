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
@Table(name = "uploads")
public class Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uploads_sequence")
    @KeywordField(
            valueBridge = @ValueBridgeRef(type = LongStringBridge.class),
            aggregable = Aggregable.YES
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id",
            foreignKey = @ForeignKey(name = "upload_report_id_fk")
    )
    private Report report;

    @ManyToOne
    @JoinColumn(name = "source_id",
            foreignKey = @ForeignKey(name = "upload_source_id_fk"))
    private Source source;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}
