package de.oscake

import com.fasterxml.jackson.databind.ObjectMapper

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file

import de.oscake.model.ComplianceArtifactCollection
import de.oscake.model.Project
import de.oscake.utils.Environment

import java.io.File
import java.io.IOException

import java.time.LocalDateTime

import kotlin.system.exitProcess

class MergerMain : CliktCommand(printHelpOnEmptyArgs = true) {

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

        var inputFileCounter = 0
        var outputFile = outputFileArg
        var archiveFile = archiveFileArg
        if (outputFileArg == null || archiveFileArg == null) {
            val fileName = cid.replace(":", ".")
            if (!isValidFilePath(fileName)) {
                Logger.log("$fileName - output file name not valid - it contains special characters!")
                require(isValidFilePath(fileName))
                return
            }
            outputFile = outputFileArg ?: File("$fileName.oscc")
            archiveFile = archiveFileArg ?: File("$fileName.zip")
        }

        // if output directory is given, strip the output files to their file names
        if (outputDirArg != null) {
            outputFile = outputDirArg!!.resolve(outputFile!!.name)
            archiveFile = outputDirArg!!.resolve(archiveFile!!.name)
        }

        val archiveFileRelativeName = makeRelativePath(outputFile!!, archiveFile!!)

        // merge all packages into new project
        val mergedProject = Project.init(ComplianceArtifactCollection(cid, archiveFileRelativeName), archiveFile)

        inputDir.walkTopDown().filter { it.isFile && it.extension == "oscc" }.forEach { file ->
            ProjectProvider.getProject(file.absoluteFile)?.let { project ->
                if (mergedProject.merge(project, file)) {
                    inputFileCounter++
                    mergedProject.hasIssues = mergedProject.hasIssues || project.hasIssues
                }
            }
        }
        Project.terminateArchiveHandling()

        val objectMapper = ObjectMapper()
        outputFile.bufferedWriter().use {
            it.write(objectMapper.writeValueAsString(mergedProject))
        }

        Logger.log("Number of processed input files: $inputFileCounter")
    }
}

    /**
     * Checks if the [path] contains invalid characters for file names
     */
    @Suppress("SwallowedException")
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
        val retFile = archiveFile.relativeToOrNull(File(outputFile.parent)) ?: archiveFile
        var retFilePath = retFile.path.replace("\\", "/")

        if (!retFilePath.startsWith("..")) retFilePath = "./$retFilePath" // indicating parent directory "../"

        return retFilePath
    }

fun main(args: Array<String>) {
    val env = Environment()
    println(env)
    Logger.log("OSCake-Merger started at: ${LocalDateTime.now()}")
    MergerMain().main(args)
    Logger.log("OSCake-Merger terminated at: ${LocalDateTime.now()}")
    exitProcess(0)
}
