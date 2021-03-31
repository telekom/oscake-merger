package model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * The class FileLicensing is a collection of [FileLicense] instances for the given path (stored in [scope])
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
internal data class FileLicensing(
    /**
     * [fileScope] contains the name of the file to which the licenses belong.
     */
    val fileScope: String,
    /**
     * Represents the path to the file containing the license text in the archive.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL) val fileContentInArchive: String?,
    /**
     * [fileLicenses] keeps a list of all license findings for this file.
     */
    val fileLicenses: MutableList<FileLicense>,
    /**
     * [fileCopyrights] keeps a list of all copyright statements for this file.
     */
    val fileCopyrights: MutableList<FileCopyright>
)
