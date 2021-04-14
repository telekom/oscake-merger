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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * A unique identifier for a software package.
 */
internal data class Identifier(
    /**
     * The type of package. When used in the context of a [Project], the type is the name of the package manager that
     * manages the project (e.g. "Gradle" for a Gradle project). When used in the context of a [Package], the type is
     * the name of the package type or protocol (e.g. "Maven" for a file from a Maven repository).
     */
    val type: String,

    /**
     * The namespace of the package, for example the group for "Maven" or the scope for "NPM".
     */
    val namespace: String,

    /**
     * The name of the package.
     */
    val name: String,

    /**
     * The version of the package.
     */
    val version: String
) {
    private constructor(components: List<String>) : this(
        type = components.getOrElse(0) { "" },
        namespace = components.getOrElse(1) { "" },
        name = components.getOrElse(2) { "" },
        version = components.getOrElse(3) { "" }
    )
    private val components = listOf(type, namespace, name, version)

    /**
     * Create an [Identifier] from a string with the format "type:namespace:name:version".
     */
    @JsonCreator
    constructor(identifier: String) : this(identifier.split(':', limit = 4))

    @JsonValue
    fun toCoordinates() = components.joinToString(":") { component -> component.trim().filterNot { it < ' ' } }
}
