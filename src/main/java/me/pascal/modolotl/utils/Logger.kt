package me.pascal.modolotl.utils

import org.slf4j.LoggerFactory

object Logger {

    private val name = "[Modolotl]"

    fun log(s: String) {
        println("$name $s")
    }

    fun error(s: String) {
        println("$name Error: $s")
    }

    fun info(s: String) {
        println("$name Info: $s")
    }

    fun warn(s: String) {
        println("$name Warning: $s")
    }

}