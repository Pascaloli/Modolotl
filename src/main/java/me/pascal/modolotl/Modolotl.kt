package me.pascal.modolotl

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.pascal.modolotl.cache.CachingHandler
import me.pascal.modolotl.command.CommandHandler
import me.pascal.modolotl.utils.Logger
import me.pascal.modolotl.utils.Settings
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Role
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

class Modolotl(args: Array<String>) {
    private val json: Json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

    private val dbFile = File("modolotl.db").absoluteFile
    private val dbUrl = "jdbc:sqlite:${dbFile.path}"

    private val settingsFile = File("settings.json")

    companion object {
        lateinit var dbConnection: Connection
        lateinit var commandHandler: CommandHandler
        lateinit var jda: JDA
        lateinit var settings: Settings
        lateinit var mutedRole: Role
        var cachingHandler = CachingHandler()
    }

    init {
        this.loadSettings()

        if (args.isEmpty() && settings.token.isEmpty()) {
            Logger.error("Token is missing")
            Logger.info("Please specify your token as an argument (Ex: java -jar bot.jar tokenhere) or in the ${settingsFile.name} file.")
            exitProcess(1)
        }

        if (args.isNotEmpty() && args[0].isNotEmpty()) {
            settings.token = args[0]
            Logger.info("Token as argument detected, using it instead of token from the ${settingsFile.name} file.")
        }

        if (settings.modRoleId.isEmpty()) {
            Logger.error("No mod role ID specified")
            Logger.info("Please specify a mod role ID in the ${settingsFile.name} file.")
            exitProcess(2)
        }

        if (settings.mutedRoleId.isEmpty()) {
            Logger.error("No muted role ID specified")
            Logger.info("Please specify a muted role ID in the ${settingsFile.name} file.")
            exitProcess(2)
        }

        if (settings.prefix.isEmpty()) {
            Logger.error("No command prefix specified")
            Logger.info("Please specify a command prefix in the ${settingsFile.name} file.")
            exitProcess(3)
        }

        initJda(settings.token)
        initDb()
        commandHandler = CommandHandler()
        mutedRole = jda.getRoleById(settings.mutedRoleId)!!
        cachingHandler.init()

        Logger.log("Initialising EventListener")
        jda.addEventListener(EventListener())
    }

    /**
     * Stringifies the [settings] object using [json] and saves it to the [settingsFile] in UTF-8.
     *
     * @author NurMarvin
     */
    private fun saveSettings() = this.settingsFile.writeText(this.json.stringify(Settings.serializer(), settings), Charsets.UTF_8)

    /**
     * Parses the contents of the [settingsFile] using [json] and assigns it to the [settings] object.
     *
     * @author NurMarvin
     */
    private fun loadSettings() {
        if(!this.settingsFile.exists()) {
            settings = Settings()
            Logger.warn("Settings file doesn't exist, creating it")
            this.saveSettings()
        }
        settings = this.json.parse(Settings.serializer(), this.settingsFile.readText(Charsets.UTF_8))
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