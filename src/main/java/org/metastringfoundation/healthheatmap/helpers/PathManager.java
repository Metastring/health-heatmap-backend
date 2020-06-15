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

package org.metastringfoundation.healthheatmap.helpers;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathManager {
    private static final Logger LOG = LogManager.getLogger(PathManager.class);

    public static String guessMetadataPath(Path path) {
        Path basedir = path.getParent();
        LOG.info("basedir is " + basedir);
        String fileName = path.getFileName().toString();
        String fileNameWithoutExtension = FilenameUtils.removeExtension(fileName);
        return Paths.get(basedir.toString(), fileNameWithoutExtension + ".metadata.json").toString();
    }

    public static String guessMetadataPath(String path) {
        return guessMetadataPath(Paths.get(path));
    }
}
