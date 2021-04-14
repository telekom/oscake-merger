/*
 * Copyright (C) 2021 Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache 
 * License, Version 2.0 (the "License"); you may not use this 
 * file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.oscake

import com.fasterxml.jackson.databind.ObjectMapper

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.file

import de.oscake.model.ComplianceArtifactCollection
import de.oscake.model.Project
import de.oscake.utils.Environment

import java.io.File
import java.io.IOException

import java.time.LocalDateTime

import kotlin.system.exitProcess

/**
 * [MergerMain] class implements the [CliktCommand] in order to handle commandline parameters
 * */
class MergerMain : CliktCommand(printHelpOnEmptyArgs = true) {

    private val inputDir by option("--inputDirectory", "-i", help = "The path to a folder containing oscc " +
            "files and their corresponding archives. May also consist of subdirectories.")
        .file(mustExist = true, canBeFile = false, canBeDir = true, mustBeWritable = false, mustBeReadable = true)
        .required()

    private val outputDirArg by option("--outputDirectory", "-o", help = "The path to the output folder.")
        .file(mustExist = true, canBeFile = false, canBeDir = true, mustBeWritable = true, mustBeReadable = true)

    private val cid by option("--cid", "-c", help = "Id of the Compliance Artifact Collection.")
        .required()

    private val outputFileArg by option("--outputFile", "-f", help = "Name of the output file. When -o is " +
            "also specified, the path to the outputFile is stripped to its name.")
        .file(mustExist = false, canBeFile = true, canBeDir = false, mustBeWritable = false, mustBeReadable = false)

    private val archiveFileArg by option("--archiveFile", "-a", help = "Name of the archive file. When -o is " +
            "also specified, the path to the archiveFile is stripped to its name.")
        .file(mustExist = false, canBeFile = true, canBeDir = false, mustBeWritable = false, mustBeReadable = false)

    private val env = Environment()

    private inner class MergerHelpFormatter : CliktHelpFormatter(requiredOptionMarker = "*", showDefaultValues = true) {
        override fun formatHelp(
            prolog: String,
            epilog: String,
            parameters: List<HelpFormatter.ParameterHelp>,
            programName: String
        ) =
            buildString {
                appendLine(getVersionHeader(env.oscakeMergerVersion))
                appendLine(super.formatHelp(prolog, epilog, parameters, programName))
                appendLine()
                appendLine("* denotes required options.")
            }
    }

    init {
        context {
            expandArgumentFiles = false
            helpFormatter = MergerHelpFormatter()
        }

        versionOption(
            version = env.oscakeMergerVersion,
            names = setOf("--version", "-v"),
            help = "Show version information and exit.",
            message = ::getVersionHeader
        )
    }

    /**
     * Executes the program, which merges oscc-files from the [inputDir] into the [outputDirArg]
     */
    @Suppress("ComplexMethod")
    override fun run() {
        println(getVersionHeader(env.oscakeMergerVersion))
        Logger.log("OSCake-Merger started at: ${LocalDateTime.now()}")

        var inputFileCounter = 0
        var outputFile = outputFileArg
        var archiveFile = archiveFileArg

        if (outputDirArg == null && (outputFileArg == null || archiveFileArg == null)) {
            Logger.log("Either <outputDirectory> and/or <outputFile> and <archiveFile> must be specified!")
            require(false)
            return
        }

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
        val env = Environment
        val release = if (env.OSCAKE_MERGER_SPECIFICATION != "") "${env.OSCAKE_MERGER_SPECIFICATION}_" +
                env.OSCAKE_MERGER_VERSION else env.OSCAKE_MERGER_VERSION
        val mergedProject = Project.init(ComplianceArtifactCollection(cid, archiveFileRelativeName,
            release), archiveFile)

        inputDir.walkTopDown().filter { it.isFile && it.extension == "oscc" }.forEach { file ->
            ProjectProvider.getProject(file.absoluteFile)?.let { project ->
                if (mergedProject.merge(project, file)) {
                    inputFileCounter++
                    val mergedFile = file.relativeToOrNull(inputDir) ?: ""
                    Logger.log("File: <$mergedFile> successfully merged!")
                }
            }
        }
        Project.terminateArchiveHandling()

        val objectMapper = ObjectMapper()
        outputFile.bufferedWriter().use {
            it.write(objectMapper.writeValueAsString(mergedProject))
        }

        Logger.log("Number of processed oscc-input files: $inputFileCounter")
        Logger.log("OSCake-Merger terminated at: ${LocalDateTime.now()}")
    }

    private fun getVersionHeader(version: String): String {
        val header = mutableListOf<String>()
        val spec = if (env.oscakeMergerVersionSpecification != "") "specification: " +
                env.oscakeMergerVersionSpecification else ""

        """
            
             XXXXX    XXXXX    XXXXX   XXXX   X    X  XXXXXX
            X     X  X        X       X    X  X   X   X
            X     X   XXXX    X       X    X  XXXX    XXXXX    -----  MERGER: version: $version
            X     X       X   X       XXXXXX  X   X   X                       $spec
             XXXXX   XXXXX     XXXXX  X    X  X    X  XXXXXX          Running on ${env.os} under Java ${env.javaVersion}
        """.trimIndent().lines().mapTo(header) { it.trimEnd() }

        return header.joinToString("\n", postfix = "\n")
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

    /**
     * Generates the relative path in relation to the [outputFile] to be stored in oscc-file
     */
    private fun makeRelativePath(outputFile: File, archiveFile: File): String {
        val retFile = archiveFile.relativeToOrNull(File(outputFile.parent)) ?: archiveFile
        var retFilePath = retFile.path.replace("\\", "/")

        if (!retFilePath.startsWith("..")) retFilePath = "./$retFilePath" // indicating parent directory "../"

        return retFilePath
    }

/**
 * The entry point for the application with [args] = the list of arguments.
 */
fun main(args: Array<String>) {
    MergerMain().main(args)
    exitProcess(0)
}
