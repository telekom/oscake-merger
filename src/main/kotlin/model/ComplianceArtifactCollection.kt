package de.oscake.model

import de.oscake.utils.OSCAKE_MERGER_ARCHIVE_TYPE
import de.oscake.utils.OSCAKE_MERGER_AUTHOR

import java.time.LocalDateTime

/**
 * The class [ComplianceArtifactCollection] contains meta information about the merged oscc file. Currently,
 * only zip archives are supported.
 */
internal data class ComplianceArtifactCollection(
    /**
     * [cid] is the Identifier of the project
     */
    val cid: String,
    /**
     * name of the authoring tool
     */
    val author: String,
    /**
     * Represents the specification/release number of the program.
     */
    val release: String,
    /**
     * [date] keeps the creation timestamp of the report.
     */
    val date: String,
    /**
     * [archivePath] describes the path to the archive file containing the processed license files.
     */
    val archivePath: String,
    /**
     * In current versions only zip files are used.
     */
    val archiveType: String
) {
    constructor(cid: String, archiveFileName: String, release: String) : this(cid, OSCAKE_MERGER_AUTHOR,
        release, LocalDateTime.now().toString(), archiveFileName, OSCAKE_MERGER_ARCHIVE_TYPE)
}
