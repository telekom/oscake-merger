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

package de.oscake.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

import model.FileLicensing

internal data class ComplianceArtifactPackage(
    /**
     * Package ID [pid]: e.g. "tdosca-tc06"  - part of the [id].
     */
    val pid: String,
    /**
     * version number of the package: e.g. "1.0" - part of the [id].
     */
    val release: String,
    /**
     * [repository] contains the URL directing to the source code repository.
     */
    val repository: String,
    /**
     * Unique identifier for the package.
     */
    val id: Identifier,
    /**
     * If the package is REUSE compliant, this flag is set to true.
     */
    @get: JsonInclude(JsonInclude.Include.NON_DEFAULT) val reuseCompliant: Boolean,
    /**
     * [hasIssues] shows that issues have happened during processing.
     */
    @get: JsonInclude(JsonInclude.Include.NON_DEFAULT) val hasIssues: Boolean,
    /**
     *  [defaultLicensings] contains a list of [DefaultLicense]s  for non-REUSE compliant packages.
     */
    val defaultLicensings: MutableList<DefaultLicense>,
    /**
     *  [dirLicensings] contains a list of [DirLicensing]s for non-REUSE compliant packages.
     */
    val dirLicensings: MutableList<DirLicensing>,
    /**
     *  This list is only filled for REUSE-compliant packages and contains a list of [DefaultLicense]s.
     */
     val reuseLicensings: MutableList<ReuseLicense>,
    /**
     *  [fileLicensings] contains a list of [fileLicensings]s.
     */
    val fileLicensings: MutableList<FileLicensing>

) {
    /**
     * [origin] contains the name of the source file and is set during deserialization
     */
    @JsonIgnore lateinit var origin: String
}
