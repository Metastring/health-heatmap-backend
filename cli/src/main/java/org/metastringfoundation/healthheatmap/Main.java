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

package org.metastringfoundation.healthheatmap;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.metastringfoundation.data.DatasetIntegrityError;

import java.io.IOException;

/**
 * Runs the application. Various CLI params available.
 *
 * @see org.metastringfoundation.healthheatmap.cli
 */
@QuarkusMain
public class Main {

    /**
     * Entry point.
     *
     * @param args - cli arguments
     * @throws IllegalArgumentException if arguments are wrong
     */
    public static void main(String[] args) throws IllegalArgumentException {
       Quarkus.run(MainWithBells.class, args);
    }

}
