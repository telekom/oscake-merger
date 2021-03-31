import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import model.ComplianceArtifactPackage
import model.Project
import model.ReuseLicense
import org.apache.logging.log4j.Level
import java.io.File
import java.io.IOException

internal class ProjectProvider private constructor(){
    companion object {
        private val mapper = jacksonObjectMapper()

        fun getProject(source: File): Project? {
            var project: Project? = null
            try {
                val json = File(source.absolutePath).readText()
                project = mapper.readValue<Project>(json)
            } catch (e: IOException) {
                Logger.log(e.stackTraceToString(), Level.ERROR)
            }
            finally {
                return project
            }
        }
    }
}