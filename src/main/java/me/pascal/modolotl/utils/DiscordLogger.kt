package me.pascal.modolotl.utils

import me.pascal.modolotl.Modolotl
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

object DiscordLogger {

    private val logChannel = Modolotl.jda.guilds[0].getTextChannelById(Modolotl.settings.logChannelId)!!

    fun logJoin(member: Member) {
        val embed = EmbedBuilder()
        embed.setAuthor("Member joined", null, member.user.effectiveAvatarUrl)
        embed.setThumbnail(member.user.effectiveAvatarUrl)
        embed.setDescription("${member.asMention} ${member.user.name}#${member.user.discriminator}")

        val createdAt = member.user.timeCreated
        val now = OffsetDateTime.now().minusDays(2)
        if (createdAt.isAfter(now)) {
            val createdAgo = "Created ${ChronoUnit.DAYS.between(createdAt, now)} day(s), " +
                    "${ChronoUnit.HOURS.between(createdAt, now)} hour(s), " +
                    "${ChronoUnit.MINUTES.between(createdAt, now)} minute(s) ago."
            embed.addField("New Account", createdAgo, true)
        }

        logChannel.sendMessage(embed.build()).queue()
    }

    fun logLeave(member: Member) {
        val embed = EmbedBuilder()
        embed.setAuthor("Member left", null, member.user.effectiveAvatarUrl)
        embed.setThumbnail(member.user.effectiveAvatarUrl)
        embed.setDescription("${member.asMention} ${member.user.name}#${member.user.discriminator}")
        logChannel.sendMessage(embed.build()).queue()
    }

    fun logRolesGiven(member: Member, addedRoles: List<Role>) {
        val embed = EmbedBuilder()
        embed.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
        embed.setDescription("${member.asMention} was **given** `${addedRoles.map { it.name }.joinToString("`,`")}` role(s)")
        logChannel.sendMessage(embed.build()).queue()
    }

    fun logRolesRemoved(member: Member, removedRoles: List<Role>) {
        val embed = EmbedBuilder()
        embed.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
        embed.setDescription("${member.asMention} was **removed** from `${removedRoles.map { it.name }.joinToString("`,`")}` role(s)")
        logChannel.sendMessage(embed.build()).queue()
    }

    fun logMute() {

    }

    fun logBan(banned: User, banner: Member, reason: String) {
        val embed = EmbedBuilder()
        embed.setAuthor("Member Banned", null, banned.effectiveAvatarUrl)
        embed.setDescription("${banned.asMention} ${banned.name}#${banned.discriminator}\n" +
                "**Banned** by ${banner.asMention}\n" +
                "Reason: $reason")
        logChannel.sendMessage(embed.build()).queue()
    }

    fun logUnban(unbanned: User, banner: Member) {
        val embed = EmbedBuilder()
        embed.setAuthor("Member Unbanned", null, unbanned.effectiveAvatarUrl)
        embed.setDescription("${unbanned.asMention} ${unbanned.name}#${unbanned.discriminator}\n" +
                "**Unbanned** by ${banner.asMention}")
        logChannel.sendMessage(embed.build()).queue()
    }

    fun logKick(kicked: User, kicker: Member) {

    }

    fun logCommand(member: Member, channel: TextChannel, command: String, raw: String) {
        val embed = EmbedBuilder()
        embed.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
        embed.setDescription("${member.asMention} executed command `$command` in ${channel.asMention}\n$raw")
        logChannel.sendMessage(embed.build()).queue()
    }
}