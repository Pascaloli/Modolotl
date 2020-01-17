package me.pascal.modolotl.command

import me.pascal.modolotl.Modolotl
import net.dv8tion.jda.api.entities.Message

open class Command(var trigger: String, val permissions: PERMISSIONS = PERMISSIONS.USER) {
    val dbConnection = Modolotl.dbConnection
    open fun handle(message: Message) {
    }
}

enum class PERMISSIONS {
    USER, MOD
}