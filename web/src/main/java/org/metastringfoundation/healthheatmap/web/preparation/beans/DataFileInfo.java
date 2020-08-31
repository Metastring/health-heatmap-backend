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

package org.metastringfoundation.healthheatmap.web.preparation.beans;

import org.metastringfoundation.data.DataPoint;

import java.util.Map;

public class DataFileInfo {
    public VerificationResult verificationResult;
    public Map<DataPoint, Map<String, String>> transformErrors;

    private DataFileInfo(VerificationResult verificationResult, Map<DataPoint, Map<String, String>> transformErrors) {
        this.verificationResult = verificationResult;
        this.transformErrors = transformErrors;
    }

    public static DataFileInfo of(VerificationResult verificationResult, Map<DataPoint, Map<String, String>> transformErrors) {
        return new DataFileInfo(verificationResult, transformErrors);
    }
}
