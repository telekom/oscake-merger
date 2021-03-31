package model

internal data class DefaultLicense(
    /**
     * Shows the [foundInFileScope] to the file where the license was found.
     */
    val foundInFileScope: String?,
    /**
     * [license] contains the name of the license.
     */
    val license: String?,
    /**
     * Represents the path to the file containing the license text in the archive.
     */
    val licenseTextInArchive: String?
)
