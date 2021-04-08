package de.oscake.model

/**
 * The FileLicense class wraps the information about the [license] and the name of the file containing the license
 * information [licenseTextInArchive]. An instance with null values may exist if the file was archived by the scanner
 * and not treated by other logic branches
 */
internal data class FileLicense(
    /**
     * [license] contains the name of the license.
     */
    val license: String?,
    /**
     * Represents the path to the file containing the license text in the archive.
     */
    var licenseTextInArchive: String? = null,
)
