package de.oscake

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import de.oscake.model.Project

import java.io.File
import java.io.IOException

import org.apache.logging.log4j.Level

internal class ProjectProvider private constructor() {
    companion object {
        private val mapper = jacksonObjectMapper()

        fun getProject(source: File): Project? {
            var project: Project? = null
            try {
                val json = File(source.absolutePath).readText()
                project = mapper.readValue<Project>(json)
                project.complianceArtifactPackages.forEach {
                    it.origin = source.name
                }
            } catch (e: IOException) {
                Logger.log(e.stackTraceToString(), Level.ERROR)
            } finally {
                return project
            }
        }
    }
}
