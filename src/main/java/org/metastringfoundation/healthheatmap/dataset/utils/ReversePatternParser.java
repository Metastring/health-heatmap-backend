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

package org.metastringfoundation.healthheatmap.dataset.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReversePatternParser {

    private static final Logger LOG = LogManager.getLogger(ReversePatternParser.class);


    /* # is literal for the #
       \\{ is escaped once for java string, and next for regex number match, and then talks about opening {
       ( starts a capture
         [ starts a class of characters
           ^} means anything but }
         ] closes the class
         * Any number of characters that belong to this class
       ) End of capture
       } literal closing brace
     */
    // unused for now
    private static final Pattern patternParsingPattern = Pattern.compile("/#\\{([^}]*)}/");

    private final Pattern pattern;

    private final List<String> orderedElements;

    public ReversePatternParser(String inputPattern, Map<String, String> replacementMap) {
        // first let us do some pre-processing of the replacementMap

        // this map to store where the keys are in the inputPattern (later used to sort the positions)
        Map<String, Integer> positions = new HashMap<>();

        for (Map.Entry<String, String> entry: replacementMap.entrySet()) {
            // first store positions
            String key = entry.getKey();
            int indexOfKey = inputPattern.indexOf(key);
            if (indexOfKey != -1) {
                positions.put(key, inputPattern.indexOf(key));
            }
        }

        LOG.debug("Positions is " + positions.toString());

        orderedElements = new ArrayList<>();
        positions.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> orderedElements.add(x.getKey()));

        LOG.debug("Ordered elements is " + orderedElements.toString());


        // now let us replace the inputpattern with replacementmap
        for (Map.Entry<String, String> entry: replacementMap.entrySet()) {
            inputPattern = inputPattern.replace(entry.getKey(), entry.getValue());
        }

        LOG.debug("Pattern created for replacement from " + inputPattern);

        // compile that replaced inputpattern
        pattern = Pattern.compile(inputPattern);
    }

    public Map<String, String> parse(String value) {
        LOG.debug("Parsing " + value);
        Matcher m = pattern.matcher(value);

        Map<String, String> output = new HashMap<>();

        if (m.find()) {
            for (int match = 1; match <= orderedElements.size(); match++) {
                String foundEntity = m.group(match);
                LOG.debug("Found " + foundEntity + " at match " + match);
                String entityKey = orderedElements.get(match - 1);
                output.put(entityKey, foundEntity);
            }
        }

        LOG.debug("Parsed as " + output);
        return output;
    }
}
