/*
 * Copyright (C) 2021 Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package model

import com.fasterxml.jackson.annotation.JsonInclude

import de.oscake.model.FileCopyright
import de.oscake.model.FileLicense

/**
 * The class FileLicensing is a collection of [FileLicense] instances for the given path (stored in [fileScope])
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
internal data class FileLicensing(
    /**
     * [fileScope] contains the name of the file to which the licenses belong.
     */
    val fileScope: String,
    /**
     * Represents the path to the file containing the license text in the archive.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL) var fileContentInArchive: String?,
    /**
     * [fileLicenses] keeps a list of all license findings for this file.
     */
    val fileLicenses: MutableList<FileLicense>,
    /**
     * [fileCopyrights] keeps a list of all copyright statements for this file.
     */
    val fileCopyrights: MutableList<FileCopyright>
)
