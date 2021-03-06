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

/**
 * The class DirLicensing is a collection of [DirLicense] instances for the given path (stored in [dirScope])
 */
internal data class DirLicensing(
    /**
     * [dirScope] contains the name of the folder to which the licenses belong.
     */
    val dirScope: String,
    /**
     * [dirLicenses] contains a list of [DirLicense]s.
     */
    val dirLicenses: List<DirLicense>
)
