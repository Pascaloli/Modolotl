package me.pascal.modolotl

import me.pascal.modolotl.utils.Logger
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventListener : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message
        if (message.author.id == message.jda.selfUser.id) return
        val possibleCommand = message.contentRaw.split(" ")[0]
        val prefix = Modolotl.settings.getPrefix()

        if (possibleCommand.startsWith(prefix)) {
            val actualCommand =
                    possibleCommand.substring(prefix.length, possibleCommand.length)
            val command = Modolotl.commandHandler.getCommand(actualCommand)
            if (command != null)
                Modolotl.commandHandler.handle(message, command)
        }


        super.onMessageReceived(event)
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val member = event.member
        Logger.debug("Member ${member.effectiveName} joined")
        val userId = member.user.id
        val cachedUser = Modolotl.cachingHandler.getUserById(userId)
        if (cachedUser != null) {
            val guild = member.guild
            cachedUser.roles.forEach { roleId ->
                val role = guild.getRoleById(roleId)
                if (role != null) {
                    if (!role.isManaged && !role.isPublicRole) {
                        event.guild.addRoleToMember(userId, role).queue()
                        Logger.debug("Added Role ${role.name} to ${member.effectiveName}")
                    }
                } else {
                    Logger.debug("Found non existing Role in Database $roleId")
                }
            }
        } else {
            Logger.debug("No cache found for User ${member.effectiveName}")
            Modolotl.cachingHandler.addUser(member, true)
        }
        super.onGuildMemberJoin(event)
    }
}