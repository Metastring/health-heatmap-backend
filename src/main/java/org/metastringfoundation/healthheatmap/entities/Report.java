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

import org.metastringfoundation.healthheatmap.helpers.URIStringConverter;

import javax.persistence.*;
import java.net.URI;

/**
 * One report (of some data) that is quoting a source
 *
 * <p>
 *     Imagine a newspaper reports "According to NFHS-4, there is 1% MMR in Champaran" that is a report of the source.
 *     Usually this report will be coming from data.gov.in pages. But these are still reports. We could even have reports
 *     coming in from the original source.
 * </p>
 * <p>
 *     Related to an upload in that there should be one upload for one report, usually.
 * </p>
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Report.findByUri",
                   query = "SELECT r from Report r WHERE r.uri = :uri")
})
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue
    private Long id;

    @Convert(converter = URIStringConverter.class)
    private URI uri;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
