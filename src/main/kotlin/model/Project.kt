package de.oscake.model

import com.fasterxml.jackson.annotation.JsonInclude
import org.apache.logging.log4j.Level

/**
 * The class [Project] wraps the meta information ([complianceArtifactCollection]) of the OSCakeReporter as well
 * as a list of included projects and packages store in instances of [Pack]
 */
internal data class Project(
    /**
     * [hasIssues] shows if problems occurred during processing the data.
     */
    var hasIssues: Boolean = false,
    /**
     * [complianceArtifactCollection] contains meta data about the project.
     */
    @get:JsonInclude(JsonInclude.Include.NON_NULL) var complianceArtifactCollection: ComplianceArtifactCollection?,
    /**
     * [complianceArtifactPackages] is a list of packages [pack] which are part of the project.
     */
    val complianceArtifactPackages: MutableList<ComplianceArtifactPackage>
)
{
    constructor(cac: ComplianceArtifactCollection ):this(false, cac, mutableListOf<ComplianceArtifactPackage>())

    fun merge(project: Project) {
        val packagesToAdd = mutableListOf<ComplianceArtifactPackage>()
        project.complianceArtifactPackages.forEach { complianceArtifactPackage ->
            if (!containsID(complianceArtifactPackage.id))
                packagesToAdd.add(complianceArtifactPackage)
            else
                inspectPackage(complianceArtifactPackage)
        }
        complianceArtifactPackages.addAll(packagesToAdd)
    }

    private fun inspectPackage(cap: ComplianceArtifactPackage) {
        var error = false
        val oriCap = this.complianceArtifactPackages.firstOrNull { it.id ==  cap.id } !!

        // same version from different source repositories?
        error = error || oriCap.repository != cap.repository
        // find differences in defaultLicensings
        error = error || oriCap.defaultLicensings.size != cap.defaultLicensings.size
        oriCap.defaultLicensings.forEach { oriDefaultLicense ->
            error = error || cap.defaultLicensings.none {
                it.foundInFileScope == oriDefaultLicense.foundInFileScope && it.license == oriDefaultLicense.license }
        }
        // find differences in dirLicensings
        if (oriCap.dirLicensings.size != cap.dirLicensings.size) error = true
        oriCap.dirLicensings.forEach { oriDirLicensing ->
            error = error || (cap.dirLicensings.none { oriDirLicensing.dirScope == it.dirScope})
            val dirLicensing = cap.dirLicensings.firstOrNull{ it.dirScope == oriDirLicensing.dirScope}
            if (dirLicensing == null) { error = true }
            else {
                error = error || (oriDirLicensing.dirLicenses.size != dirLicensing.dirLicenses.size)
                oriDirLicensing.dirLicenses.forEach { oriDirLicense ->
                    error = error || (dirLicensing.dirLicenses.none {
                        oriDirLicense.license == it.license && oriDirLicense.foundInFileScope == it.foundInFileScope })
                }
            }
        }
        // find differences in reuseLicensings
        error = error || oriCap.reuseLicensings.size != cap.reuseLicensings.size
        oriCap.reuseLicensings.forEach { oriReuseLicense ->
            error = error || cap.reuseLicensings.none{ it.foundInFileScope == oriReuseLicense.foundInFileScope
                    && it.license == oriReuseLicense.license}
        }
        // find differences in fileLicensings
        error = error || (oriCap.fileLicensings.size != cap.fileLicensings.size)
        oriCap.fileLicensings.forEach { oriFileLicensing ->
            error = error || (cap.fileLicensings.none { oriFileLicensing.fileScope == it.fileScope})
            val fileLicensing = cap.fileLicensings.firstOrNull{ it.fileScope == oriFileLicensing.fileScope}
            if (fileLicensing == null) { error = true }
            else {
                error = error || (oriFileLicensing.fileLicenses.size != fileLicensing.fileLicenses.size)
                oriFileLicensing.fileLicenses.forEach { oriFileLicense ->
                    error = error || (fileLicensing.fileLicenses.none { oriFileLicense.license == it.license })
                }
                error = error ||  (oriFileLicensing.fileCopyrights.size != fileLicensing.fileCopyrights.size)
                oriFileLicensing.fileCopyrights.forEach { oriFileCopyright ->
                    error = error || (fileLicensing.fileCopyrights.none { oriFileCopyright.copyright == it.copyright })
                }
            }
        }

        if (error)
            Logger.log("[${oriCap.origin}: ${cap.id}]: difference(s) in file  ${cap.origin}!", Level.WARN)
    }

    private fun containsID(id: Identifier): Boolean = this.complianceArtifactPackages.any { it.id == id}

}