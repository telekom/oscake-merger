package de.oscake

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Logger private constructor() {
    companion object {
        /**
         * The [theLogger] is a reference to the Apache log4j2.
         */
        private val theLogger: Logger = LogManager.getLogger()

        fun log(msg: String, level: Level = Level.INFO) {
            theLogger.log(level, msg)
        }
    }
}
