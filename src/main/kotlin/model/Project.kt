package de.oscake.model

import de.oscake.Logger
import com.fasterxml.jackson.annotation.JsonInclude
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.IOUtils
import org.apache.logging.log4j.Level
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry


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
    companion object {
        lateinit var zipOutput: ArchiveOutputStream //= ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, zipOutputStream)
        lateinit var zipOutputStream: FileOutputStream //= FileOutputStream(archiveFile)
        var isInitialProject = false
        var project: Project? = null
        var archiveFile: File? = null

        fun init(cac: ComplianceArtifactCollection, arcFile: File): Project {
            if (project != null) return project!!

            project = Project(false, cac, mutableListOf<ComplianceArtifactPackage>())
            isInitialProject = true
            archiveFile = arcFile
            zipOutputStream = FileOutputStream(arcFile)
            zipOutput = ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, zipOutputStream)

            return project!!
        }
    }

    private val filesToArchive = mutableListOf<String>()
    private constructor(cac: ComplianceArtifactCollection):this(false, cac, mutableListOf<ComplianceArtifactPackage>())


    fun terminateArchiveHandling() {
        if (isInitialProject) {
            zipOutput.finish()
            zipOutputStream.close();
        }
    }


    fun merge(project: Project) {
        if (!isInitialProject) return
        val packagesToAdd = mutableListOf<ComplianceArtifactPackage>()
        val prefix = getNewPrefix(project)
        project.complianceArtifactPackages.forEach { complianceArtifactPackage ->
            if (!containsID(complianceArtifactPackage.id)) {
                packagesToAdd.add(complianceArtifactPackage)
                adjustFilePaths(complianceArtifactPackage, prefix, project)
//                copyFromArchiveToArchive(prefix,
//                "C:\\reinhardt\\firma\\Hasenzagl\\Magenta\\working\\merger\\input\\tdosca-tc06.zip",
//                    "C:\\reinhardt\\firma\\Hasenzagl\\Magenta\\working\\merger\\output\\neu2.zip")
            }
            else
                inspectPackage(complianceArtifactPackage)
        }
        if (project.filesToArchive.size > 0)
            copyFromArchiveToArchive(project, prefix,
                "C:\\reinhardt\\firma\\Hasenzagl\\Magenta\\working\\merger\\input\\tdosca-tc06.zip")
                //"C:\\reinhardt\\firma\\Hasenzagl\\Magenta\\working\\merger\\output\\neu2.zip")

        complianceArtifactPackages.addAll(packagesToAdd)
    }

    private fun copyFromArchiveToArchive(project: Project, prefix: String, zipInputPath: String /*, zipOutputPath: String*/) {

        try {
            val zipInput = ZipFile(File(zipInputPath))
            //val zipOutputStream: OutputStream = FileOutputStream(File(zipOutputPath))
            //val zipOutput = ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, zipOutputStream)

            zipInput.use { zip ->
                val entries: Enumeration<ZipArchiveEntry> = zip.entries
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!project.filesToArchive.contains(entry.name))
                        continue
                    val newEntry = ZipEntry(prefix + entry.name)
                    newEntry.method = entry.method
                    zipOutput.putArchiveEntry(ZipArchiveEntry(newEntry))
                    IOUtils.copy(zipInput.getInputStream(entry), zipOutput)
                    println(prefix + ": " + entry.name)
                    zipOutput.closeArchiveEntry()
                }
            }
//            zipOutput.finish()
//            zipOutputStream.close();
        } catch (ex: Exception) {
            Logger.log("Error when copying zip file from <$zipInputPath> to <${archiveFile!!.name}>: ${ex.toString()}", Level.ERROR)
        }
    }


    private fun adjustFilePaths(complianceArtifactPackage: ComplianceArtifactPackage, prefix: String, project: Project) {
        complianceArtifactPackage.defaultLicensings.forEach {
            if (it.licenseTextInArchive != null) {
                project.filesToArchive.add(it.licenseTextInArchive!!)
                it.licenseTextInArchive = "$prefix${it.licenseTextInArchive}"
            }
        }
        complianceArtifactPackage.reuseLicensings.forEach {
            if (it.licenseTextInArchive != null) {
                project.filesToArchive.add(it.licenseTextInArchive!!)
                it.licenseTextInArchive = "$prefix${it.licenseTextInArchive}"
            }
        }
        complianceArtifactPackage.dirLicensings.forEach { dirLicensing ->
            dirLicensing.dirLicenses.forEach {
                if (it.licenseTextInArchive != null) {
                    project.filesToArchive.add(it.licenseTextInArchive!!)
                    it.licenseTextInArchive = "$prefix${it.licenseTextInArchive}"
                }
            }
        }
        complianceArtifactPackage.fileLicensings.forEach { fileLicensing ->
            if (fileLicensing.fileContentInArchive != null) {
                project.filesToArchive.add(fileLicensing.fileContentInArchive!!)
                fileLicensing.fileContentInArchive = "$prefix${fileLicensing.fileContentInArchive}"
            }
            fileLicensing.fileLicenses.forEach {
                if (it.licenseTextInArchive != null) {
                    project.filesToArchive.add(it.licenseTextInArchive!!)
                    it.licenseTextInArchive = "$prefix${it.licenseTextInArchive}"
                }
            }
        }
    }


    private fun getNewPrefix(project: Project): String {
        return project.complianceArtifactCollection.hashCode().toString()+"-"
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