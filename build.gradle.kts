import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val cliktVersion: String by project
val jacksonVersion: String by project
val log4jCoreVersion: String by project

plugins {
    kotlin("jvm") version "1.4.10"
}
group = "de.oscake"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jCoreVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jCoreVersion")


}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}