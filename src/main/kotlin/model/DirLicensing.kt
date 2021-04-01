package de.oscake.model

/**
 * The class DirLicensing is a collection of [DirLicense] instances for the given path (stored in [scope])
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