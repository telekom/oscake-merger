package de.oscake.model

/**
 * A class representing a license item for REUSE packages.
 */
internal class ReuseLicense(
    /**
     * Path to the corresponding file (source).
     */
    val foundInFileScope: String?,
    /**
     * [license] keeps the name of the license.
     */
    val license: String?,
    /**
     * Path to the license file in the archive (target)
     */
    var licenseTextInArchive: String? = null
)