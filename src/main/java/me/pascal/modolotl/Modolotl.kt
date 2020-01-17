package me.pascal.modolotl

import me.pascal.modolotl.utils.Logger
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

class Modolotl(args: Array<String>) {

    private val dbFile = File("modolotl.db").absoluteFile
    private val dbUrl = "jdbc:sqlite:${dbFile.path}"

    companion object {
        lateinit var dbConnection: Connection
        lateinit var jda: JDA
    }

    init {
        if (args.isEmpty()) {
            Logger.error("Token is missing")
            Logger.info("Specify your token as an argument (Ex: java -jar bot.jar tokenhere)")
            exitProcess(1)
        }
        val token = args[0]

        initJda(token)
        initDb()
    }

    private fun initJda(token: String) {
        try {
            Logger.log("Initialising JDA")
            jda = JDABuilder(AccountType.BOT).setToken(token).build().awaitReady()

            if(jda.guilds.size > 1){
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
            Logger.log("Initialising Datebase Driver")
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
                    "CREATE TABLE users IF NOT EXIST (" +
                            "userId INTEGER PRIMARY KEY, " +
                            "roles TEXT, " +
                            "mutedAt INTEGER, " +
                            "mutedUntil INTEGER, " +
                            "mutedBy INTEGER, " +
                            "bannedBy INTEGER);" +
                            "" +
                            "CREATE UNIQUE INDEX table_name_userid_uindex " +
                            "ON table_name (userid);"
            dbConnection.createStatement().execute(createTableQuery)
        }catch(e: Exception){
            Logger.error("Couldn't initialise Database Tables")
        }
    }


}