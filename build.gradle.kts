import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val cliktVersion: String by project
val jacksonVersion: String by project
val log4jCoreVersion: String by project
val osCakeMergerVersion: String by project
val osCakeApplication: String by project
val commonsCompressVersion: String by project

plugins {
    kotlin("jvm") version "1.4.10"
    application
}
group = "de.oscake"
version = "$osCakeMergerVersion"

application {
    applicationName = "$osCakeApplication"
    mainClassName = "de.oscake.MergerMain"
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