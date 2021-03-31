package model

import com.fasterxml.jackson.annotation.JsonInclude

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
        // find differences in defaultLicensings
        if (oriCap.defaultLicensings.size != cap.defaultLicensings.size) error = true
        oriCap.defaultLicensings.forEach { oriDefaultLicense ->
            val defaultLicense = cap.defaultLicensings.firstOrNull{ it.foundInFileScope == oriDefaultLicense.foundInFileScope}
            if (defaultLicense == null) {
                error = true }
            else {
                if (oriDefaultLicense.license != defaultLicense.license) error = true
            }
        }

/*
        oriCap.defaultLicensings.forEach { oriDefaultLicense ->
            if (!cap.defaultLicensings.any { oriDefaultLicense.license == it.license &&
                    oriDefaultLicense.foundInFileScope == it.foundInFileScope })
                error = true
        }*/
        // find differences in dirLicensings
        if (oriCap.dirLicensings.size != cap.dirLicensings.size) error = true
        oriCap.dirLicensings.forEach { oriDirLicensing ->
            if (cap.dirLicensings.none { oriDirLicensing.dirScope == it.dirScope}) error = true
            val dirLicensing = cap.dirLicensings.firstOrNull{ it.dirScope == oriDirLicensing.dirScope}
            if (dirLicensing == null) {
                error = true }
            else {
                if (oriDirLicensing.dirLicenses.size != dirLicensing.dirLicenses.size) error = true
                oriDirLicensing.dirLicenses.forEach { oriDirLicense ->
                    if (dirLicensing.dirLicenses.none {
                        oriDirLicense.license == it.license &&
                                oriDirLicense.foundInFileScope == it.foundInFileScope
                    }) error = true
                }
            }
        }
        // find differences in reuseLicensings
        if (oriCap.reuseLicensings.size != cap.reuseLicensings.size) error = true
        oriCap.reuseLicensings.forEach { oriReuseLicense ->
            val reuseLicense = cap.reuseLicensings.firstOrNull{ it.foundInFileScope == oriReuseLicense.foundInFileScope}
            if (reuseLicense == null) {
                error = true }
            else {
                if (oriReuseLicense.license != reuseLicense.license) error = true
            }
        }
        // find differences in fileLicensings
        if (oriCap.fileLicensings.size != cap.fileLicensings.size) error = true
        oriCap.fileLicensings.forEach { oriFileLicensing ->
            if (cap.fileLicensings.none { oriFileLicensing.fileScope == it.fileScope}) error = true
            val fileLicensing = cap.fileLicensings.firstOrNull{ it.fileScope == oriFileLicensing.fileScope}
            if (fileLicensing == null) {
                error = true }
            else {
                if (oriFileLicensing.fileLicenses.size != fileLicensing.fileLicenses.size) error = true
                oriFileLicensing.fileLicenses.forEach { oriFileLicense ->
                    if (fileLicensing.fileLicenses.none {
                            oriFileLicense.license == it.license }) error = true
                }

                if (oriFileLicensing.fileCopyrights.size != fileLicensing.fileCopyrights.size) error = true
                oriFileLicensing.fileCopyrights.forEach { oriFileCopyright ->
                    if (fileLicensing.fileCopyrights.none {
                            oriFileCopyright.copyright == it.copyright }) error = true
                }
            }
        }


        if (error)
            Logger.log("[${cap.id}] already exists in merged file but is different!")
    }

    private fun containsID(id: Identifier): Boolean = this.complianceArtifactPackages.any { it.id == id}

}