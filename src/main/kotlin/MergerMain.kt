import com.fasterxml.jackson.databind.ObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import model.Project
import java.time.LocalDateTime
import kotlin.system.exitProcess

class MergerMain : CliktCommand(printHelpOnEmptyArgs = true) {
    private val projects = mutableListOf<Project>()

    private val inputDir by option("--inputDirectory", "-i", help = "The path to a folder containing oscc files.")
        .file(mustExist = true, canBeFile = false, canBeDir = true, mustBeWritable = false, mustBeReadable = true)
        .required()

    private val outputDir by option("--outputDirectory", "-o", help = "The path to the output folder.")
        .file(mustExist = true, canBeFile = false, canBeDir = true, mustBeWritable = true, mustBeReadable = true)
        .required()

    override fun run() {
        inputDir.walkTopDown().filter { it.isFile && it.extension == "oscc" }.forEach { file ->
            ProjectProvider.getProject(file.absoluteFile)?.let {
                projects.add(it)
            }
        }
        if (projects.size == 0) {
            Logger.log("No files found to merge!")
            return
        }

        // merge all packages into first project
        val mergedProject = Project()
        projects.forEach {
            mergedProject.merge(it)
            mergedProject.hasIssues = mergedProject.hasIssues || it.hasIssues
        }

/*        for (i in 1 until projects.size) {
            projects[0].merge(projects[i])
            projects[0].hasIssues = projects[0].hasIssues || projects[i].hasIssues
        }*/
        // temporarily set to null --> ignore this property
        //projects[0].complianceArtifactCollection = null


        val objectMapper = ObjectMapper()
        val outputFile = outputDir.resolve("merged.oscc")
        outputFile.bufferedWriter().use {
            it.write(objectMapper.writeValueAsString(mergedProject))
        }


        Logger.log("Number of processed input files: ${projects.size}")
    }
}

fun main(args: Array<String>) {
    Logger.log("OSCake-Merger started at: ${LocalDateTime.now().toString()}")
    MergerMain().main(args)
    Logger.log("OSCake-Merger terminated at: ${LocalDateTime.now().toString()}")
    exitProcess(0)
}