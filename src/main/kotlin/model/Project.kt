package de.oscake.model

import com.fasterxml.jackson.annotation.JsonInclude

import de.oscake.Logger

import java.io.File
import java.io.FileOutputStream

import java.security.MessageDigest

import java.util.Enumeration
import java.util.zip.ZipEntry

import kotlin.system.exitProcess

import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.IOUtils

import org.apache.logging.log4j.Level

/**
 * The class [Project] wraps the meta information ([complianceArtifactCollection]) of the OSCakeReporter as well
 * as a list of included projects and packages stored in instances of [Project]
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
     * [complianceArtifactPackages] is a list of packages [complianceArtifactPackages] which are part of the project.
     */
    val complianceArtifactPackages: MutableList<ComplianceArtifactPackage>
) {
    companion object {
        private lateinit var zipOutput: ArchiveOutputStream
        private lateinit var zipOutputStream: FileOutputStream
        private var initProject: Project? = null
        private var archiveFile: File? = null

        // see https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#messagedigest-algorithms
        private val DIGEST by lazy { MessageDigest.getInstance("SHA-1") }

        fun init(cac: ComplianceArtifactCollection, arcFile: File): Project {
            if (initProject != null) return initProject!!

            initProject = Project(false, cac, mutableListOf())
            initProject!!.isInitialProject = true
            archiveFile = arcFile
            try {
                zipOutputStream = FileOutputStream(arcFile)
                zipOutput = ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, zipOutputStream)
            } catch (ex: ArchiveException) {
                Logger.log("Error when handling the archive file <$archiveFile> - Abnormal program termination! " +
                        ex.toString(), Level.ERROR)
                exitProcess(2)
            }

            return initProject!!
        }

        fun terminateArchiveHandling() {
            if (initProject != null) {
                try {
                    zipOutput.finish()
                    zipOutputStream.close()
                } catch (ex: ArchiveException) {
                    Logger.log("Error when terminating the archive file <$archiveFile> handling - Abnormal " +
                            "program termination! " + ex.toString(), Level.ERROR)
                    exitProcess(2)
                }
            }
        }
    }

    private var isInitialProject = false

    @Suppress("ReturnCount")
    fun merge(project: Project, originFile: File): Boolean {
        // merge only for [init]ialized project allowed
        if (!isInitialProject) return false
        // do not process, if definitions in [complianceArtifactCollection] or itself are missing
        if (project.complianceArtifactCollection == null) return false

        val packagesToAdd = mutableListOf<ComplianceArtifactPackage>()
        val filesToArchive = mutableListOf<String>()
        val prefix = getNewPrefix(project)
        var absoluteFilePathToZip: File?
        val originDir = originFile.parentFile

        if (project.complianceArtifactCollection == null) {
            Logger.log("Incomplete <complianceArtifactCollection> in file: <${originFile.name}>", Level.WARN)
            return false
        } else {
            absoluteFilePathToZip = originDir.resolve(project.complianceArtifactCollection!!.archivePath)
            if (!absoluteFilePathToZip.exists()) {
                Logger.log("Archive file <$absoluteFilePathToZip> for project in <${originFile.name}> does " +
                        "not exist!", Level.WARN)
                return false
            }
        }

        project.complianceArtifactPackages.forEach { complianceArtifactPackage ->
            if (!containsID(complianceArtifactPackage.id)) {
                packagesToAdd.add(complianceArtifactPackage)
                adjustFilePaths(complianceArtifactPackage, prefix, filesToArchive)
            } else {
                inspectPackage(complianceArtifactPackage)
            }
        }
        if (filesToArchive.size > 0) copyFromArchiveToArchive(filesToArchive, prefix, absoluteFilePathToZip)
        if (packagesToAdd.size > 0) complianceArtifactPackages.addAll(packagesToAdd)

        return true
    }

    @Suppress("NestedBlockDepth")
    private fun copyFromArchiveToArchive(
        filesToArchive: List<String>,
        prefix: String,
        absoluteFilePathToZip: File
    ): Boolean {
        var rc = true
        try {
            val zipInput = ZipFile(absoluteFilePathToZip)
            zipInput.use { zip ->
                val entries: Enumeration<ZipArchiveEntry> = zip.entries
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!filesToArchive.contains(entry.name)) continue
                    val newEntry = ZipEntry(prefix + entry.name)
                    newEntry.method = entry.method
                    zipOutput.putArchiveEntry(ZipArchiveEntry(newEntry))
                    IOUtils.copy(zipInput.getInputStream(entry), zipOutput)
                    zipOutput.closeArchiveEntry()
                }
            }
        } catch (ex: ArchiveException) {
            Logger.log("Error when copying zip file from <$absoluteFilePathToZip> to <${archiveFile!!.name}>: " +
                    ex.toString(), Level.ERROR)
            rc = false
        } finally {
            return rc
        }
    }

    private fun adjustFilePaths(
        complianceArtifactPackage: ComplianceArtifactPackage,
        prefix: String,
        filesToArchive: MutableList<String>
    ) {
        complianceArtifactPackage.defaultLicensings.forEach {
            if (it.licenseTextInArchive != null) {
                filesToArchive.add(it.licenseTextInArchive!!)
                it.licenseTextInArchive = "$prefix${it.licenseTextInArchive}"
            }
        }
        complianceArtifactPackage.reuseLicensings.forEach {
            if (it.licenseTextInArchive != null) {
                filesToArchive.add(it.licenseTextInArchive!!)
                it.licenseTextInArchive = "$prefix${it.licenseTextInArchive}"
            }
        }
        complianceArtifactPackage.dirLicensings.forEach { dirLicensing ->
            dirLicensing.dirLicenses.forEach {
                if (it.licenseTextInArchive != null) {
                    filesToArchive.add(it.licenseTextInArchive!!)
                    it.licenseTextInArchive = "$prefix${it.licenseTextInArchive}"
                }
            }
        }
        complianceArtifactPackage.fileLicensings.forEach { fileLicensing ->
            if (fileLicensing.fileContentInArchive != null) {
                filesToArchive.add(fileLicensing.fileContentInArchive!!)
                fileLicensing.fileContentInArchive = "$prefix${fileLicensing.fileContentInArchive}"
            }
            fileLicensing.fileLicenses.forEach {
                if (it.licenseTextInArchive != null) {
                    filesToArchive.add(it.licenseTextInArchive!!)
                    it.licenseTextInArchive = "$prefix${it.licenseTextInArchive}"
                }
            }
        }
    }

    private fun getNewPrefix(project: Project): String {
        val key = project.complianceArtifactCollection!!.cid
        return DIGEST.digest(key.toByteArray()).joinToString("") { String.format("%02x", it) } + "-"
    }

    @Suppress("ComplexMethod")
    private fun inspectPackage(cap: ComplianceArtifactPackage) {
        var error = false
        val oriCap = this.complianceArtifactPackages.firstOrNull { it.id == cap.id }!!

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
            error = error || (cap.dirLicensings.none { oriDirLicensing.dirScope == it.dirScope })
            val dirLicensing = cap.dirLicensings.firstOrNull { it.dirScope == oriDirLicensing.dirScope }
            if (dirLicensing == null) { error = true } else {
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
            error = error || cap.reuseLicensings.none { it.foundInFileScope == oriReuseLicense.foundInFileScope &&
                it.license == oriReuseLicense.license }
        }
        // find differences in fileLicensings
        error = error || (oriCap.fileLicensings.size != cap.fileLicensings.size)
        oriCap.fileLicensings.forEach { oriFileLicensing ->
            error = error || (cap.fileLicensings.none { oriFileLicensing.fileScope == it.fileScope })
            val fileLicensing = cap.fileLicensings.firstOrNull { it.fileScope == oriFileLicensing.fileScope }
            if (fileLicensing == null) { error = true } else {
                error = error || (oriFileLicensing.fileLicenses.size != fileLicensing.fileLicenses.size)
                oriFileLicensing.fileLicenses.forEach { oriFileLicense ->
                    error = error || (fileLicensing.fileLicenses.none { oriFileLicense.license == it.license })
                }
                error = error || (oriFileLicensing.fileCopyrights.size != fileLicensing.fileCopyrights.size)
                oriFileLicensing.fileCopyrights.forEach { oriFileCopyright ->
                    error = error || (fileLicensing.fileCopyrights.none { oriFileCopyright.copyright == it.copyright })
                }
            }
        }

        if (error) Logger.log("[${oriCap.origin}: ${cap.id}]: difference(s) in file ${cap.origin}!", Level.WARN)
    }

    private fun containsID(id: Identifier): Boolean = this.complianceArtifactPackages.any { it.id == id }
}
