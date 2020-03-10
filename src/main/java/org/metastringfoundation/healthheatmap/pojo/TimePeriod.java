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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public class TimePeriod {

    @Column(name="start_time")
    @JsonProperty("start")
    private LocalDate startTime;

    @Column(name="end_time")
    @JsonProperty("end")
    private LocalDate endTime;

    public TimePeriod() {}

    public TimePeriod(LocalDate startTime, LocalDate endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public LocalDate getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDate endTime) {
        this.endTime = endTime;
    }
}
