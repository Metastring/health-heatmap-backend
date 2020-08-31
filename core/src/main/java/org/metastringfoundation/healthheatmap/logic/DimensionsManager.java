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

package org.metastringfoundation.healthheatmap.logic;

import org.metastringfoundation.healthheatmap.helpers.HealthDataset;
import org.metastringfoundation.healthheatmap.storage.beans.ValuePointAssociation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DimensionsManager {
    void refresh() throws IOException;

    Set<String> getValidIdsOf(String dimension);

    Boolean idExists(String dimension, String id);

    Boolean dimensionExists(String dimension);

    void createDimension(String dimension);

    void addValuesToDimension(String dimension, Map<String, Map<String, String>> values);

    List<HealthDataset> augmentDatasetsWithDimensionInfo(Collection<HealthDataset> datasets);

    HealthDataset augmentDatasetWithDimensionInfo(HealthDataset dataset);

    // TODO: Use a graph database here
    void persistAssociationWithIndicator(Collection<HealthDataset> datasets);

    Map<String, List<String>> fieldsAssociatedWithIndicator(String indicator);

    List<String> getAllIndicatorsWithAssociations();

    List<ValuePointAssociation> getDimensionAssociationsOf(List<? extends Map<String, String>> data);

    List<String> getKnownDimensions();
}
