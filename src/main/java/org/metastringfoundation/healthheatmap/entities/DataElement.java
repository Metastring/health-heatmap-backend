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

import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.PropertyBinderRef;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.metastringfoundation.healthheatmap.helpers.bridges.SettlementValueBridge;
import org.metastringfoundation.healthheatmap.helpers.bridges.TimePeriodDatesBinder;
import org.metastringfoundation.healthheatmap.helpers.bridges.UploadIdBridge;

import javax.persistence.*;

@Entity
@Indexed
@Table(name = "dataelements")
public class DataElement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataelements_sequence")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "indicator_id",
            foreignKey = @ForeignKey(name = "data_element_indicator_id_fk")
    )
    @IndexedEmbedded
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Indicator indicator;

    @ManyToOne
    @JoinColumn(name = "geography_id",
            foreignKey = @ForeignKey(name = "data_element_geography_id_fk")
    )
    @IndexedEmbedded
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Geography geography;

    @ManyToOne
    @JoinColumn(name = "upload_id",
                foreignKey = @ForeignKey(name = "data_element_upload_id_fk")
    )
    @GenericField(
            valueBridge = @ValueBridgeRef(type = UploadIdBridge.class)
    )
    private Upload upload;

    @ManyToOne
    @JoinColumn(name = "report_id",
            foreignKey = @ForeignKey(name = "data_element_report_id_fk")
    )
    @IndexedEmbedded
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Report report;

    @ManyToOne
    @JoinColumn(name = "source_id",
            foreignKey = @ForeignKey(name = "data_element_source_id_fk")
    )
    @IndexedEmbedded
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Source source;

    @Embedded
    @PropertyBinding(binder = @PropertyBinderRef(type = TimePeriodDatesBinder.class))
    private TimePeriod timePeriod;

    @Embedded
    @GenericField(
            valueBridge = @ValueBridgeRef(type = SettlementValueBridge.class)
    )
    private Settlement settlement;

    @GenericField
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public void setIndicator(Indicator indicator) {
        this.indicator = indicator;
    }

    public Geography getGeography() {
        return geography;
    }

    public void setGeography(Geography geography) {
        this.geography = geography;
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
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

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
