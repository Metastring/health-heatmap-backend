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

package org.metastringfoundation.healthheatmap.dataset;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class UnmatchedReport {
    private URI uri;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public UnmatchedReport(URI uri) {
        this.uri = uri;
    }

    public UnmatchedReport() {}

    public UnmatchedReport(String uri) throws DatasetIntegrityError {
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new DatasetIntegrityError("Metadata has an invalid uri: " + uri);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnmatchedReport that = (UnmatchedReport) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public String toString() {
        return "UnmatchedReport{" +
                "uri=" + uri +
                '}';
    }
}
