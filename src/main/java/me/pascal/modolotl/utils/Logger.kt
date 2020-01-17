package me.pascal.modolotl.utils

import org.slf4j.LoggerFactory

object Logger {
    private val logger: org.slf4j.Logger = LoggerFactory.getLogger("Modolotl")

    fun log(s: String) = this.logger.info(s)

    fun error(s: String) = this.logger.error(s)

    fun info(s: String) = this.log(s)

    fun warn(s: String) = this.logger.warn(s)

    fun debug(s: String) = this.logger.debug(s)
}