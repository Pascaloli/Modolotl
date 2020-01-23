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

class MuteCommand : Command("mute", CommandPermission.MOD) {
    override fun handle(message: Message) {

        val arguments = message.contentRaw.split(" ", limit = 4)
        val userToModerate = findUserToModerate(message, arguments) ?: return
        val memberToModerate = message.guild.getMemberById(userToModerate.id)

        if (memberToModerate == null) {
            message.channel.sendMessage("$noEmote **_${userToModerate.name}#${userToModerate.discriminator} is not on this guild_   **").queue()
            return
        }

        val cache = Modolotl.cachingHandler.getUserById(userToModerate.id)!!

        try {
            if (cache.isMuted()) {
                //unmute
                message.guild.removeRoleFromMember(memberToModerate, Modolotl.mutedRole).queue()
                //Modolotl.cachingHandler.updateMute(memberToModerate, message.author, null, null, cache)
                message.channel.sendMessage("$yesEmote ${userToModerate.name}#${userToModerate.discriminator} has been unmuted.").queue()
                DiscordLogger.logUnmute(memberToModerate, message.author)
                return
            } else {
                if (arguments.size < 3) {
                    message.guild.addRoleToMember(memberToModerate, Modolotl.mutedRole).queue()
                    Modolotl.cachingHandler.updateMute(memberToModerate, message.author, null, null, cache)
                    message.channel.sendMessage("$yesEmote ${userToModerate.name}#${userToModerate.discriminator} has been muted permanently with no given reason.").queue()
                    DiscordLogger.logMute(memberToModerate, message.member!!, "", "permanently")
                    return
                } else {
                    //duration or single word reason
                    val firstPart = arguments[2]
                    val additionalReason = if (arguments.size == 4) arguments[3] else ""
                    //check if first part is also reason or duration
                    val durationExtensions = arrayOf("s", "m", "h", "d", "y")
                    val hasDuration = firstPart.substring(0, firstPart.length - 1).toLongOrNull() != null
                            && durationExtensions.contains(firstPart.substring(firstPart.length - 1, firstPart.length))
                    val finalReason = if (hasDuration) additionalReason else "${arguments[2]}${if (additionalReason.isEmpty()) "" else " "}$additionalReason"
                    var prettyDuration: String? = null
                    val duration = if (hasDuration) {
                        val durPart = firstPart.substring(0, firstPart.length - 1).toLong()
                        val extPart = firstPart.substring(firstPart.length - 1, firstPart.length)

                        if (durPart == 0L) {
                            message.channel.sendMessage("$noEmote Duration cant be 0").queue()
                            return
                        } else if (durPart < 0) {
                            message.channel.sendMessage("$noEmote Duration cant be negative").queue()
                            return
                        }

                        when (extPart) {
                            "s" -> {
                                prettyDuration = "$durPart Seconds"
                                durPart * 1000
                            }
                            "m" -> {
                                prettyDuration = "$durPart Minutes"
                                durPart * 1000 * 60
                            }
                            "h" -> {
                                prettyDuration = "$durPart Hours"
                                durPart * 1000 * 60 * 60
                            }
                            "d" -> {
                                prettyDuration = "$durPart Days"
                                durPart * 1000 * 60 * 60 * 24
                            }
                            "y" -> {
                                prettyDuration = "$durPart Years"
                                durPart * 1000 * 60 * 60 * 24 * 365

                            }
                            else -> {
                                0
                            }
                        }
                    } else {
                        0
                    }

                    message.guild.addRoleToMember(memberToModerate, Modolotl.mutedRole).queue()
                    Modolotl.cachingHandler.updateMute(memberToModerate, message.author, System.currentTimeMillis(), System.currentTimeMillis() + duration, cache)
                    message.channel.sendMessage("$yesEmote ${userToModerate.asMention} has been muted ${if (prettyDuration != null) "for $prettyDuration" else "permanently"} ${if (finalReason.isEmpty()) "with no given reason" else "for `$finalReason`"}").queue()
                    DiscordLogger.logMute(memberToModerate, message.member!!, finalReason, if (prettyDuration != null) "$prettyDuration" else "permanently")
                    return
                }
            }
        } catch (ex: InsufficientPermissionException) {
            message.channel.sendMessage("$noEmote Missing permission `MANAGE_ROLES`").queue()
            return
        } catch (ex: HierarchyException) {
            message.channel.sendMessage("$noEmote Cannot ban this user").queue()
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