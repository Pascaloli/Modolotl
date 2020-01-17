package me.pascal.modolotl.utils

import org.slf4j.LoggerFactory

object Logger {

    private val name = "[Modolotl]"
    private var debug = false

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

    fun debug(s: String) {
        if (debug)
            println("$name Debug: $s")
    }

    fun setDebug(debug: Boolean) {
        this.debug = debug
    }

}