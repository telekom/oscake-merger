package de.oscake

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Logger private constructor(){
    companion object {
        /**
         * The [logger] is a reference to the Apache log4j2.
         */
        private val logger: Logger = LogManager.getLogger()

        fun log(msg: String, level: Level = Level.INFO ) {
            logger.log(level, msg)
        }
    }
}
