package model

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
     * Create an [Identifier] from a string with the format "type:namespace:name:version". If the string has less than
     * three colon separators the missing values are assigned empty strings.
     */
    @JsonCreator
    constructor(identifier: String) : this(identifier.split(':', limit = 4))

    @JsonValue
    fun toCoordinates() = components.joinToString(":") { component -> component.trim().filterNot { it < ' ' } }

}