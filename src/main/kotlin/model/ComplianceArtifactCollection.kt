package model

/**
 * The class [ComplianceArtifactCollection] contains meta information about the run of the OSCakeReporter. Currently,
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
     * Represents the release number of the OSCakeReporter which was used when creating the file.
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
    val archiveType:String
)