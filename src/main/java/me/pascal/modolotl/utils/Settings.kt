package me.pascal.modolotl.utils

import java.io.File

class Settings {

    private val settingsFile = File("modolotl.txt").absoluteFile
    private var token = ""
    private var modRole = ""
    private var prefix = ""


    init {
        checkFile()
        readFile()
    }

    private fun checkFile() {
        if (!settingsFile.exists() || settingsFile.readLines().isEmpty()) {
            Logger.warn("Settings file doesn't exist, creating it")
            settingsFile.parentFile.mkdirs()
            settingsFile.createNewFile()
            saveFile()
        }
    }

    private fun saveFile() {
        settingsFile.writeText("token:$token\nmodrole:$modRole\nprefix:$prefix")
    }

    private fun readFile() {
        settingsFile.readLines().forEach { line ->
            val lineSplit = line.split(":")
            if (!lineSplit[1].isEmpty()) {
                if (lineSplit[0] == "token") {
                    this.token = lineSplit[1]
                    Logger.log("Found Token in Settings file.")
                } else if (lineSplit[0] == "modrole") {
                    this.modRole = lineSplit[1]
                    Logger.log("Found Modrole in Settings file.")
                } else if (lineSplit[0] == "prefix") {
                    this.prefix = lineSplit[1]
                    Logger.log("Found Prefix in Settings file.")
                }
            }
        }
    }

    fun getToken(): String {
        return token
    }

    fun getModRole(): String {
        return modRole
    }

    fun setToken(token: String) {
        this.token = token
    }

    fun getPrefix(): String {
        return prefix
    }

}