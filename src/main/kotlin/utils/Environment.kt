package de.oscake.utils

import java.lang.Runtime

/**
 * A description of the environment that OSCake-Merger was executed in.
 */
data class Environment(
    /**
     * The version of OSCake-Merger used.
     */
    val oscakeMergerVersion: String = OSCAKE_MERGER_VERSION,

    /**
     * The version of Java used.
     */
    val javaVersion: String = System.getProperty("java.version"),

    /**
     * Name of the operating system.
     */
    val os: String = System.getProperty("os.name").orEmpty(),

    /**
     * The number of logical processors available.
     */
    val processors: Int = Runtime.getRuntime().availableProcessors(),

    /**
     * The maximum amount of memory available.
     */
    val maxMemory: Long = Runtime.getRuntime().maxMemory(),
)
