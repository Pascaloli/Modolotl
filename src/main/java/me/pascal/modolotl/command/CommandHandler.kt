package me.pascal.modolotl.command

import me.pascal.modolotl.Modolotl
import me.pascal.modolotl.command.commands.BanCommand
import me.pascal.modolotl.command.commands.KickCommand
import me.pascal.modolotl.command.commands.MuteCommand
import me.pascal.modolotl.command.commands.RoleCommand
import me.pascal.modolotl.utils.Logger
import net.dv8tion.jda.api.entities.Message

class CommandHandler {

    private val commands = arrayListOf(
            RoleCommand(), BanCommand(), KickCommand(), MuteCommand()
    )

    fun getCommand(name: String): Command? {
        return commands.find { it.trigger.equals(name, ignoreCase = true) }
    }

    fun getCommands(): ArrayList<Command> {
        return commands
    }

    fun handle(message: Message, command: Command) {
        val author = message.member!!
        if (command.permissions == PERMISSIONS.MOD) {
            if (author.roles.map { it.id }.contains(Modolotl.settings.getModRole())) {
                command.handle(message)
                Logger.warn("${author.effectiveName} - (Mod) executed '${message.contentRaw}'")
            } else {
                Logger.warn("${author.effectiveName} tried to execute '${message.contentRaw}'")
            }
        } else {
            command.handle(message)
            Logger.warn("${author.effectiveName} executed '${message.contentRaw}'")

        }
    }
}