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

package org.metastringfoundation.healthheatmap.web.utils;

import org.jboss.logging.Logger;
import org.metastringfoundation.healthheatmap.beanconverters.FilterToDataQuery;
import org.metastringfoundation.healthheatmap.beans.Filter;
import org.metastringfoundation.healthheatmap.beans.FilterAndSelectFields;
import org.metastringfoundation.healthheatmap.helpers.ListAndMapUtils;
import org.metastringfoundation.healthheatmap.logic.Application;
import org.metastringfoundation.healthheatmap.storage.beans.DataQuery;
import org.metastringfoundation.healthheatmap.storage.beans.DataQueryResult;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class AppInteraction {
    private static final Logger LOG = Logger.getLogger(AppInteraction.class);
    private final Application app;

    @Inject
    public AppInteraction(Application app) {
        this.app = app;
    }

    public List<Map<String, String>> getDataForDownload(FilterAndSelectFields filtersAndFields) throws IOException {
        verifyAndFixFiltersCrashingIfInappropriate(filtersAndFields);
        if (filtersAndFields.getFilter().isPresent()) {
            DataQueryResult queryResult = app.query(FilterToDataQuery.convert(filtersAndFields.getFilter().get()));
            List<Map<String, String>> filtered;
            if (filtersAndFields.getFields() != null) {
                filtered = ListAndMapUtils.filterKeys(queryResult.getResult(), filtersAndFields.getFields());
            } else {
                filtered = queryResult.getResult();
            }
            return filtered;
        }
        throw new WebApplicationException(ErrorCreator.getPublicViewableError("The query without filters can lead to extremely large results. Aborting."));
    }

    private void verifyAndFixFiltersCrashingIfInappropriate(FilterAndSelectFields filtersAndFields) {
        if (filtersAndFields.getFilter().isEmpty()) {
            throw new WebApplicationException(ErrorCreator.getPublicViewableError("Should give filters"));
        }
        if (filtersAndFields.getFields() != null && !filtersAndFields.getFields().contains("value")) {
            filtersAndFields.getFields().add("value");
        }
        if (filtersAndFields.getFilter().isEmpty()) {
            throw new WebApplicationException(ErrorCreator.getPublicViewableError("The query without filters can lead to extremely large results. Aborting."));
        }
    }

    public DataQueryResult query(DataQuery query) throws IOException {
        return app.query(query);
    }

    private static Optional<Double> getDoubleOptionally(String s) {
        if (s == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public List<Map<String, String>> getScores(Filter filter) throws IOException {
        ensureFilterIsGood(filter);
        ensureOnlySingleValueForEntityIndicatorCombination(filter);
        List<Map<String, String>> data = getDataWithAppropriateSelections(filter);
        Map<String, List<Map<String, String>>> groupedByDimension = groupByIndicatorForFindingMinMaxOfIndicator(data);
        Map<String, Double> minimums = findMinimumOfEachIndicator(groupedByDimension);
        Map<String, Double> maximums = findMaximumOfEachIndicator(groupedByDimension);
        failIfMinMaxCalculationHasFailures(minimums, maximums);

        return data.stream()
                .collect(Collectors.groupingBy(e -> e.get("entity.id"), Collectors.mapping(Function.identity(), Collectors.toList())))
                .entrySet().stream()
                .map(entry -> computeCompositeAndCollapse(entry, minimums, maximums))
                .collect(Collectors.toList());
    }

    private void failIfMinMaxCalculationHasFailures(Map<String, Double> minimums, Map<String, Double> maximums) {
        if (minimums.containsValue(Double.NEGATIVE_INFINITY) || maximums.containsValue(Double.POSITIVE_INFINITY)) {
            throw ErrorCreator.getErrorFor("Finding minimum/maximum failed");
        }
    }

    @Nonnull
    private Map<String, Double> findMaximumOfEachIndicator(Map<String, List<Map<String, String>>> groupedByDimension) {
        // we use positive infinity and negative infinity (in the other method) to denote that there is no meaningful maximum (or minimum)
        return groupedByDimension.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), getMaximumOf(getValues(entry.getValue())).orElse(Double.POSITIVE_INFINITY)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Nonnull
    private Map<String, Double> findMinimumOfEachIndicator(Map<String, List<Map<String, String>>> groupedByDimension) {
        return groupedByDimension.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), getMinimumOf(getValues(entry.getValue())).orElse(Double.NEGATIVE_INFINITY)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, List<Map<String, String>>> groupByIndicatorForFindingMinMaxOfIndicator(List<Map<String, String>> data) {
        return data.stream()
                .collect(Collectors.groupingBy(m -> m.get("indicator.id")));
    }

    private List<Map<String, String>> getDataWithAppropriateSelections(Filter filter) throws IOException {
        FilterAndSelectFields filterAndSelectFields = new FilterAndSelectFields();
        filterAndSelectFields.setFilter(filter);
        filterAndSelectFields.setFields(List.of("entity.id", "indicator.Positive/Negative", "indicator.id", "value"));
        return getDataForDownload(filterAndSelectFields);
    }

    private void ensureOnlySingleValueForEntityIndicatorCombination(Filter filter) throws IOException {
        // We want to ensure that there won't be two values for any indicator-entity combination
        Map<String, List<String>> possibleDimensions = app.getFieldsPossibleAt(filter);
        possibleDimensions.remove("indicator.id");
        possibleDimensions.remove("entity.id"); // these are expected to be multiple values possible
        for (Map.Entry<String, List<String>> entry : possibleDimensions.entrySet()) {
            if (entry.getValue().size() > 1) {
                throw ErrorCreator.getErrorFor("Please filter a single value for " + entry.getKey() + " from "
                        + String.join(", ", entry.getValue()));
            }
        }
    }

    private void ensureFilterIsGood(Filter filter) {
        if (filter.getTerms() == null) {
            throw ErrorCreator.getErrorFor("Please specify a filter");
        }
        filter.getTerms().computeIfAbsent("indicator.Positive/Negative", whatever -> new ArrayList<>()).addAll(List.of(
                "POSITIVE", "NEGATIVE"
        ));
    }

    private Map<String, String> computeCompositeAndCollapse(
            Map.Entry<String, List<Map<String, String>>> entry,
            Map<String, Double> minimums,
            Map<String, Double> maximums
    ) {
        List<Double> scores = entry.getValue().stream()
                .map(dp -> getScoreFor(dp.get("value"), minimums.get(dp.get("indicator.id")), maximums.get(dp.get("indicator.id")), dp.get("indicator.Positive/Negative")))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        String compositeScore = "NA";
        try {
            compositeScore = String.valueOf(scores.stream().reduce((double) 0, Double::sum) / scores.size());
        } catch (ArithmeticException e) {
            LOG.trace("Arithmetic exception occurred for dimension " + "indicator.id");
        }
        Map<String, String> result = new LinkedHashMap<>();
        result.put("entity.id", entry.getKey());
        entry.getValue().forEach(m -> result.put(m.get("indicator.id"), m.get("value")));
        result.put("Composite Score", compositeScore);
        return result;
    }

    private Optional<Double> getScoreFor(String valueString, Double minimum, Double maximum, String type) {
        return getDoubleOptionally(valueString).map(value -> (maximum - value) / (maximum - minimum)).map(d -> {
            if (type.equals("NEGATIVE")) {
                return 1 - d;
            } else {
                return d;
            }
        });
    }

    private List<String> getValues(List<Map<String, String>> dataPoints) {
        return dataPoints.stream().map(dp -> dp.get("value")).collect(Collectors.toList());
    }

    private Optional<Double> getMinimumOf(List<String> values) {
        return values.stream()
                .map(AppInteraction::getDoubleOptionally)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Double::compare);
    }

    private Optional<Double> getMaximumOf(List<String> values) {
        return values.stream()
                .map(AppInteraction::getDoubleOptionally)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Double::compare);
    }
}
