package me.pascal.modolotl.command.commands

import me.pascal.modolotl.Modolotl
import me.pascal.modolotl.command.Command
import me.pascal.modolotl.command.CommandPermission
import me.pascal.modolotl.command.findUserToModerate
import me.pascal.modolotl.utils.DiscordLogger
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.sql.SQLException

class BanCommand : Command("ban", CommandPermission.MOD) {
    override fun handle(message: Message) {
        val arguments = message.contentRaw.split(" ", limit = 3)
        val userToModerate = findUserToModerate(message, arguments) ?: return
        val cache = Modolotl.cachingHandler.getUserById(userToModerate.id)!!
        val reason = if(arguments.size == 2) "" else arguments[2]
        try {
            if (cache.isBanned()) {
                message.guild.unban(userToModerate).queue()
                DiscordLogger.logBan(userToModerate, message.member!!, reason)
            } else {
                message.guild.ban(userToModerate, 7, reason).queue()
                DiscordLogger.logUnban(userToModerate, message.member!!)
            }
            Modolotl.cachingHandler.updateBan(userToModerate, message.member!!)
            message.channel.sendMessage("$yesEmote " +
                    "**_${userToModerate.name}#${userToModerate.discriminator} was ${if(cache.isBanned()) "banned" else "unbanned"}_**").queue()
        } catch (ex: InsufficientPermissionException){
            message.channel.sendMessage("$noEmote Missing permission `BAN_MEMBERS`").queue()
        } catch(ex: HierarchyException) {
            message.channel.sendMessage("$noEmote Cannot ban this user").queue()
        } catch(ex: SQLException) {
            message.channel.sendMessage("$noEmote Database error occured, please check console.").queue()
            ex.printStackTrace()
        }
    }
}