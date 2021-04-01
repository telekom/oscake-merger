package de.oscake.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import model.FileLicensing

//@JsonInclude(JsonInclude.Include.NON_DEFAULT)
internal data class ComplianceArtifactPackage (
    /**
     * Package ID [pid]: e.g. "tdosca-tc06"  - part of the [id].
     */
    val pid: String,
    /**
     * version number of the package: e.g. "1.0" - part of the [id].
     */
    val release: String,
    /**
     * [repository] contains the URL directing to the source code repository.
     */
    val repository: String,
    /**
     * Unique identifier for the package.
     */
    val id: Identifier,
    /**
     * If the package is REUSE compliant, this flag is set to true.
     */
    @get: JsonInclude(JsonInclude.Include.NON_DEFAULT) public val reuseCompliant: Boolean,
    /**
     * [hasIssues] shows that issues have happened during processing.
     */
    @get: JsonInclude(JsonInclude.Include.NON_DEFAULT) val hasIssues: Boolean,
    /**
     *  [defaultLicensings] contains a list of [DefaultLicense]s  for non-REUSE compliant packages.
     */
    val defaultLicensings: MutableList<DefaultLicense>,
    /**
     *  [dirLicensings] contains a list of [DirLicensing]s for non-REUSE compliant packages.
     */
    val dirLicensings: MutableList<DirLicensing>,
    /**
     *  This list is only filled for REUSE-compliant packages and contains a list of [DefaultLicense]s.
     */
     val reuseLicensings: MutableList<ReuseLicense>,
    /**
     *  [fileLicensings] contains a list of [fileLicensings]s.
     */
    val fileLicensings: MutableList<FileLicensing>

)
{
    /**
     * [origin] contains the name of the source file
     */
    @JsonIgnore lateinit var origin: String
}