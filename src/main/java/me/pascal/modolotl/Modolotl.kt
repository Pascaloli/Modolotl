package me.pascal.modolotl

import me.pascal.modolotl.cache.CachingHandler
import me.pascal.modolotl.command.CommandHandler
import me.pascal.modolotl.utils.Logger
import me.pascal.modolotl.utils.Settings
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

class Modolotl(args: Array<String>) {

    private val dbFile = File("modolotl.db").absoluteFile
    private val dbUrl = "jdbc:sqlite:${dbFile.path}"


    companion object {
        lateinit var dbConnection: Connection
        lateinit var commandHandler: CommandHandler
        lateinit var jda: JDA
        lateinit var settings: Settings

        var cachingHandler = CachingHandler()
    }

    init {
        settings = Settings()

        if (args.isEmpty() && settings.getToken().isEmpty()) {
            Logger.error("Token is missing")
            Logger.info("Please specify your token as an argument (Ex: java -jar bot.jar tokenhere)")
            Logger.info("Or in the modolotl.txt file")
            exitProcess(1)
        }

        if(args[0].isNotEmpty()){
            settings.setToken(args[0])
            Logger.info("Token as argument detected, using this instead of the Settings file")
        }

        if(settings.getModRole().isEmpty()){
            Logger.error("No Modrole specified")
            Logger.info("Please specify a Modrole in the modolotl.txt File using its ID")
            exitProcess(2)
        }

        if(settings.getPrefix().isEmpty()){
            Logger.error("No Prefix specified")
            Logger.info("Please specify a Prefix in the modolotl.txt File")
            exitProcess(3)
        }

        initJda(settings.getToken())
        initDb()
        commandHandler = CommandHandler()
        cachingHandler.init()

        Logger.log("Initialising EventListener")
        jda.addEventListener(EventListener())
    }

    private fun initJda(token: String) {
        try {
            Logger.log("Initialising JDA")
            jda = JDABuilder(AccountType.BOT).setToken(token).setGuildSubscriptionsEnabled(true).build().awaitReady()

            if (jda.guilds.size > 1) {
                Logger.warn("The Bot is on more than one Server, this may lead to problems.")
            }
        } catch (le: LoginException) {
            Logger.error("Login Exception occured, please check your token.")
            exitProcess(1)
        } catch (iae: IllegalArgumentException) {
            Logger.error("Empty token entered")
            exitProcess(1)
        }
    }

    private fun initDb() {
        try {
            Logger.log("Initialising Database Driver")
            Class.forName("org.sqlite.JDBC")
        } catch (e: Exception) {
            Logger.error("Couldn't initialise Database Driver")
            exitProcess(1)
        }

        try {
            Logger.log("Initialising Datebase Connection")
            dbConnection = DriverManager.getConnection(dbUrl)
        } catch (e: Exception) {
            Logger.error("Couldn't initialise Database Connection")
            exitProcess(1)
        }

        try {
            Logger.log("Initialising Database Tables")
            val createTableQuery =
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "userId INTEGER PRIMARY KEY, " +
                            "roles TEXT, " +
                            "mutedAt INTEGER, " +
                            "mutedUntil INTEGER, " +
                            "mutedBy INTEGER, " +
                            "bannedBy INTEGER);" +
                            "" +
                            "CREATE UNIQUE INDEX IF NOT EXISTS table_name_userid_uindex " +
                            "ON table_name (userid);"
            dbConnection.createStatement().execute(createTableQuery)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.error("Couldn't initialise Database Tables")
        }
    }


}