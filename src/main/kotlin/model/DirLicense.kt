package model

/**
 * The DirLicense class wraps the information about the [license] the name of the file containing the license
 * information [licenseTextInArchive] and the corresponding [path]
 */
internal data class DirLicense(
    /**
     * Shows the [foundInFileScope] to the file where the license was found.
     */
    val foundInFileScope: String? = null,
    /**
     * [license] contains the name of the license.
     */
    val license: String,
    /**
     * Represents the path to the file containing the license text in the archive.
     */
    val licenseTextInArchive: String? = null
)