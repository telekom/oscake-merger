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

import de.oscake.utils.OSCAKE_MERGER_ARCHIVE_TYPE
import de.oscake.utils.OSCAKE_MERGER_AUTHOR

import java.time.LocalDateTime

/**
 * The class [ComplianceArtifactCollection] contains meta information about the merged oscc file. Currently,
 * only zip archives are supported.
 */
internal data class ComplianceArtifactCollection(
    /**
     * [cid] is the Identifier of the project
     */
    val cid: String,
    /**
     * name of the authoring tool
     */
    val author: String,
    /**
     * Represents the specification/release number of the program.
     */
    val release: String,
    /**
     * [date] keeps the creation timestamp of the report.
     */
    val date: String,
    /**
     * [archivePath] describes the path to the archive file containing the processed license files.
     */
    val archivePath: String,
    /**
     * In current versions only zip files are used.
     */
    val archiveType: String
) {
    constructor(cid: String, archiveFileName: String, release: String) : this(cid, OSCAKE_MERGER_AUTHOR,
        release, LocalDateTime.now().toString(), archiveFileName, OSCAKE_MERGER_ARCHIVE_TYPE)
}
