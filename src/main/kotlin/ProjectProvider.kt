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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import de.oscake.model.Project

import java.io.File
import java.io.IOException

import org.apache.logging.log4j.Level

/**
 * [ProjectProvider] returns a [Project]-instance - deserialized from the provided source file
 */
internal class ProjectProvider private constructor() {
    companion object {
        private val mapper = jacksonObjectMapper()

        fun getProject(source: File): Project? {
            var project: Project? = null
            try {
                val json = File(source.absolutePath).readText()
                project = mapper.readValue<Project>(json)
                // store the originating file name inside of the project instance for collision detection
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
