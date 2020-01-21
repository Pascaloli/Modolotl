package me.pascal.modolotl.command.commands

import me.pascal.modolotl.command.Command
import me.pascal.modolotl.command.CommandPermission
import me.pascal.modolotl.command.findUserToModerate
import me.pascal.modolotl.utils.DiscordLogger
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.sql.SQLException

class KickCommand : Command("kick", CommandPermission.MOD) {
    override fun handle(message: Message) {
        val arguments = message.contentRaw.split(" ")
        println("123")
        val userToKick = findUserToModerate(message, arguments) ?: return
        println("456")
        val memberToKick = message.guild.getMemberById(userToKick.id)
        try {
            if (memberToKick != null) {
                message.guild.kick(memberToKick).queue()
                message.channel.sendMessage("$yesEmote **_${userToKick.name}#${userToKick.discriminator} was kicked_**").queue()
                DiscordLogger.logKick(userToKick, message.member!!)
            } else {
                message.channel.sendMessage("$noEmote **_${userToKick.name}#${userToKick.discriminator} is not on this guild_   **").queue()
            }
        } catch (ex: InsufficientPermissionException) {
            message.channel.sendMessage("$noEmote Missing permission `KICK_MEMBERS`").queue()
        } catch (ex: HierarchyException) {
            message.channel.sendMessage("$noEmote Cannot kick this user").queue()
        }
    }
}