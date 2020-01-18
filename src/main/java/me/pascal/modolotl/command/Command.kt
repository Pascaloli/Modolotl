package me.pascal.modolotl.command

import me.pascal.modolotl.Modolotl
import net.dv8tion.jda.api.entities.Message

abstract class Command(var trigger: String, val permission: CommandPermission = CommandPermission.USER) {
    val dbConnection = Modolotl.dbConnection

    abstract fun handle(message: Message)
}

enum class CommandPermission {
    USER, MOD
}