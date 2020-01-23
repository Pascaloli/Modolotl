package me.pascal.modolotl.command.commands

import me.pascal.modolotl.Modolotl
import me.pascal.modolotl.command.Command
import me.pascal.modolotl.command.CommandPermission
import me.pascal.modolotl.command.findUserToModerate
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.sql.SQLException

class RoleCommand : Command("role", CommandPermission.MOD) {
    override fun handle(message: Message) {
        val arguments = message.contentRaw.split(" ", limit = 3)
        val userToModerator = findUserToModerate(message, arguments) ?: return
        val memberToModerator = message.guild.getMemberById(userToModerator.id)

        if (memberToModerator == null) {
            message.channel.sendMessage("$noEmote **_${userToModerator.name}#${userToModerator.discriminator} is not on this guild_   **").queue()
            return
        }
        if (arguments.size < 3) {
            message.channel.sendMessage("$noEmote Missing Argument.").queue()
            return
        }

        val possibleRole = arguments[2]
        val roles = message.guild.getRolesByName(possibleRole, false)
        if (roles.isEmpty()) {
            message.channel.sendMessage("$noEmote No Role found named `$possibleRole`").queue()
            return
        }
        val role = roles[0]
        if(role.isManaged || role.isPublicRole){
            message.channel.sendMessage("$noEmote Cannot manage this role `$possibleRole`").queue()
            return
        }
        try {
            if (memberToModerator.roles.contains(role)) {
                message.guild.removeRoleFromMember(memberToModerator, role).queue()
                message.channel.sendMessage("$yesEmote Removed `${role.name}` from ${userToModerator.name}#${userToModerator.discriminator}").queue()
            } else {
                message.guild.addRoleToMember(memberToModerator, role).queue()
                message.channel.sendMessage("$yesEmote Added `${role.name}` to ${userToModerator.name}#${userToModerator.discriminator}").queue()
            }
            Modolotl.cachingHandler.updateRoles(memberToModerator)
        } catch (ex: InsufficientPermissionException) {
            message.channel.sendMessage("$noEmote Missing permission `MANAGE_ROLES`").queue()
            return
        } catch (ex: HierarchyException) {
            message.channel.sendMessage("$noEmote Cannot manage this role.").queue()
            return
        } catch (ex: SQLException) {
            message.channel.sendMessage("$noEmote Database error occured, please check console.").queue()
            ex.printStackTrace()
            return
        } catch (ex: Exception) {
            message.channel.sendMessage("$noEmote Error occured, please check console.").queue()
            ex.printStackTrace()
            return
        }
    }
}