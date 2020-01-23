package me.pascal.modolotl

import me.pascal.modolotl.utils.DiscordLogger
import me.pascal.modolotl.utils.Logger
import me.pascal.modolotl.utils.UnmuteTasks
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventListener : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message
        if (message.author.id == message.jda.selfUser.id) return
        val possibleCommand = message.contentRaw.split(" ")[0]
        val prefix = Modolotl.settings.prefix

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
        val userId = member.user.id

        Logger.debug("Member ${member.effectiveName} joined")
        DiscordLogger.logJoin(member)

        //check if user exists in DB/Cache, if so reassign roles
        val cachedUser = Modolotl.cachingHandler.getUserById(userId)
        if (cachedUser != null && cachedUser.roles.isNotEmpty()) {
            val guild = member.guild
            cachedUser.roles.filter { it.isNotBlank() }.forEach { roleId ->
                println("lool: $roleId")
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

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        DiscordLogger.logLeave(event.member)
        super.onGuildMemberLeave(event)
    }

    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent) {
        Modolotl.cachingHandler.updateRoles(event.member)
        DiscordLogger.logRolesGiven(event.member, event.roles)
        super.onGuildMemberRoleAdd(event)
    }

    override fun onGuildMemberRoleRemove(event: GuildMemberRoleRemoveEvent) {
        Modolotl.cachingHandler.updateRoles(event.member)
        DiscordLogger.logRolesRemoved(event.member, event.roles)
        if (event.roles.contains(Modolotl.mutedRole)) {
            UnmuteTasks.getTaskById(event.member.id)?.cancel()
            Modolotl.cachingHandler.updateMute(event.member, Modolotl.jda.selfUser, null, null, Modolotl.cachingHandler.getUserById(event.member.id)!!)
        }
        super.onGuildMemberRoleRemove(event)
    }


}