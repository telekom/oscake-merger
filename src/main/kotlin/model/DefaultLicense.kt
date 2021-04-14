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

internal data class DefaultLicense(
    /**
     * Shows the path:[foundInFileScope] to the file where the license was found.
     */
    val foundInFileScope: String?,
    /**
     * [license] contains the name of the license.
     */
    val license: String?,
    /**
     * Represents the path to the file containing the license text in the archive.
     */
    var licenseTextInArchive: String?
)
