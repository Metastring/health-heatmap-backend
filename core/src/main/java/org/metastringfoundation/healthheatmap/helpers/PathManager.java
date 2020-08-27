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
import org.jboss.logging.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathManager {
    private static final Logger LOG = Logger.getLogger(PathManager.class);

    public static Path guessMetadataPath(Path path) {
        Path basedir = path.getParent();
        LOG.debug("basedir is " + basedir);
        String fileName = path.getFileName().toString();
        String fileNameWithoutExtension = FileManager.dropExtension(fileName);
        return basedir.resolve(fileNameWithoutExtension + ".metadata.json");
    }

    public static Path guessRootMetadataPath(Path path) {
        Path basedir = path.getParent();
        return basedir.resolve("metadata.json");
    }

    public static Boolean isInsideOrSameAsPath(Path possibleChild, Path possibleParent) {
        Path parent = possibleParent.normalize();
        Path child = possibleChild.normalize();
        return child.getNameCount() >= parent.getNameCount() && child.startsWith(parent);
    }
}
