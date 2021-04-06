package de.oscake

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import de.oscake.model.ComplianceArtifactCollection
import de.oscake.model.Project
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import kotlin.system.exitProcess

class MergerMain : CliktCommand(printHelpOnEmptyArgs = true) {
    private val projects = mutableListOf<Project>()

    private val inputDir by option("--inputDirectory", "-i", help = "The path to a folder containing oscc files.")
        .file(mustExist = true, canBeFile = false, canBeDir = true, mustBeWritable = false, mustBeReadable = true)
        .required()

    private val outputDirArg by option("--outputDirectory", "-o", help = "The path to the output folder.")
        .file(mustExist = true, canBeFile = false, canBeDir = true, mustBeWritable = true, mustBeReadable = true)

    private val cid by option("--cid", "-c", help = "Id of the Compliance Artifact Collection")
        .required()

    private val outputFileArg by option("--outputFile", "-f", help = "Name of the output file")
        .file(mustExist = false, canBeFile = true, canBeDir = false, mustBeWritable = false, mustBeReadable = false)

    private val archiveFileArg by option("--archiveFile", "-a", help = "Name of the archive file")
        .file(mustExist = false, canBeFile = true, canBeDir = false, mustBeWritable = false, mustBeReadable = false)


    override fun run() {
        var outputFile = outputFileArg
        var archiveFile = archiveFileArg
        if (outputFileArg == null || archiveFileArg == null) {
            val fileName = cid.replace(":", ".")
            if (!isValidFilePath(fileName)) {
                Logger.log("$fileName - output file name not valid - it contains special characters!")
                require (isValidFilePath(fileName))
                return
            }
            outputFile = outputFileArg?: File("$fileName.oscc")
            archiveFile = archiveFileArg?: File("$fileName.zip")
        }

        // if output directory is given, strip the output files to their file names
        if (outputDirArg != null) {
            outputFile = outputDirArg!!.resolve(outputFile!!.name)
            archiveFile = outputDirArg!!.resolve(archiveFile!!.name)
        }

        inputDir.walkTopDown().filter { it.isFile && it.extension == "oscc" }.forEach { file ->
            ProjectProvider.getProject(file.absoluteFile)?.let {
                projects.add(it)
            }
        }
        if (projects.size == 0) {
            Logger.log("No files found to merge!")
            return
        }

        // merge all packages into new project
        val archiveFileRelativeName =  makeRelativePath(outputFile!!, archiveFile!!)

        val mergedProject = Project.init(ComplianceArtifactCollection(cid, archiveFileRelativeName), archiveFile )

        projects.forEach {
            mergedProject.merge(it)
            mergedProject.hasIssues = mergedProject.hasIssues || it.hasIssues
        }
        mergedProject.terminateArchiveHandling()

        val objectMapper = ObjectMapper()
        outputFile.bufferedWriter().use {
            it.write(objectMapper.writeValueAsString(mergedProject))
        }


        Logger.log("Number of processed input files: ${projects.size}")

    }
}

    /**
     * Checks if the [path] contains invalid characters for file names
     */
    private fun isValidFilePath(path: String): Boolean {
        val f = File(path)
        return try {
            f.canonicalPath
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun makeRelativePath(outputFile: File, archiveFile: File): String {
        var retFile = archiveFile.relativeToOrNull(File(outputFile.parent))?:archiveFile
        var retFilePath = retFile.path.replace("\\", "/")

        if (!retFilePath.startsWith("..")) // indicating parent directory "../"
            retFilePath = "./$retFilePath"
        return retFilePath
    }

fun main(args: Array<String>) {
    Logger.log("OSCake-Merger started at: ${LocalDateTime.now().toString()}")
    MergerMain().main(args)
    Logger.log("OSCake-Merger terminated at: ${LocalDateTime.now().toString()}")
    exitProcess(0)
}