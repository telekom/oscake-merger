import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val cliktVersion: String by project
val jacksonVersion: String by project
val log4jCoreVersion: String by project
val osCakeMergerVersion: String by project
val osCakeApplication: String by project
val commonsCompressVersion: String by project
val osCakeMergerSpecification: String by project

plugins {
    kotlin("jvm") version "1.4.10"
    application
}

buildscript {
    dependencies {
        // For some reason "jgitVersion" needs to be declared here instead of globally.
        val jgitVersion: String by project
        classpath("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    }
}

if (version == Project.DEFAULT_VERSION) {
    version = org.eclipse.jgit.api.Git.open(rootDir).use { git ->
        // Make the output exactly match "git describe --abbrev=7 --always --tags --dirty", which is what is used in
        // "scripts/docker_build.sh".
        val description = git.describe().setAlways(true).setTags(true).call()
        val isDirty = git.status().call().hasUncommittedChanges()

        if (isDirty) "$description-dirty" else description
    }
}

logger.quiet("Building OSCake-Merger: specification: $osCakeMergerSpecification - version $version.")

application {
    applicationName = "$osCakeApplication"
    mainClassName = "de.oscake.MergerMainKt"
}

tasks.withType<Jar>().configureEach {
    manifest {
        val versionCandidates = listOf(project.version, rootProject.version)
        attributes["Implementation-Version"] = versionCandidates.find {
            it != Project.DEFAULT_VERSION
        } ?: "GRADLE-SNAPSHOT"

        attributes["Specification-Version"] = "$osCakeMergerSpecification"
    }
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        // Work around the command line length limit on Windows when passing the classpath to Java, see
        // https://github.com/gradle/gradle/issues/1989#issuecomment-395001392.
        windowsScript.writeText(windowsScript.readText().replace(Regex("set CLASSPATH=.*"),
            "set CLASSPATH=%APP_HOME%\\\\lib\\\\*"))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jCoreVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jCoreVersion")
    implementation("org.apache.commons:commons-compress:$commonsCompressVersion")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
