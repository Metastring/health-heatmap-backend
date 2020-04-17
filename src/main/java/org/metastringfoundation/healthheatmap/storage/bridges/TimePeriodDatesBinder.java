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

package org.metastringfoundation.healthheatmap.storage.bridges;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.types.IndexFieldType;
import org.hibernate.search.mapper.pojo.bridge.PropertyBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.PropertyBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.PropertyBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.PropertyBridgeWriteContext;
import org.metastringfoundation.healthheatmap.entities.TimePeriod;

import java.time.LocalDate;

public class TimePeriodDatesBinder implements PropertyBinder {
    @Override
    public void bind(PropertyBindingContext context) {
        context.getDependencies()
                .use("startTime")
                .use("endTime");


        IndexFieldType<LocalDate> dateFieldType = context.getTypeFactory()
                .asLocalDate().toIndexFieldType();

        IndexFieldReference<LocalDate> startTime = context.getIndexSchemaElement()
                .field("start_time", dateFieldType).toReference();
        IndexFieldReference<LocalDate> endTime = context.getIndexSchemaElement()
                .field("end_time", dateFieldType).toReference();

        context.setBridge( new Bridge(
                startTime,
                endTime
        ));
    }

    private static class Bridge implements PropertyBridge {
        private final IndexFieldReference<LocalDate> startTime;
        private final IndexFieldReference<LocalDate> endTime;

        private Bridge(IndexFieldReference<LocalDate> startTime, IndexFieldReference<LocalDate> endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        public void write(DocumentElement target, Object bridgedElement, PropertyBridgeWriteContext context) {
            TimePeriod timePeriod = (TimePeriod) bridgedElement;

            if (timePeriod != null) {
                target.addValue(this.startTime, timePeriod.getStartTime());
                target.addValue(this.endTime, timePeriod.getEndTime());
            } else {
                target.addValue(this.startTime, null);
                target.addValue(this.endTime, null);
            }
        }
    }
}
