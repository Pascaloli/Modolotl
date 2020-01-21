package me.pascal.modolotl.command

import me.pascal.modolotl.Modolotl
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import java.lang.Exception
import java.lang.NumberFormatException

abstract class Command(var trigger: String, val permission: CommandPermission = CommandPermission.USER) {
    val dbConnection = Modolotl.dbConnection
    val yesEmote = Modolotl.jda.guilds[0].getEmotesByName("yes", true)[0].asMention
    val noEmote = Modolotl.jda.guilds[0].getEmotesByName("no", true)[0].asMention

    abstract fun handle(message: Message)
}

fun findUserToModerate(message: Message, arguments: List<String>): User? {
    try {
        when {
            message.mentionedMembers.isNotEmpty() -> {
                //By mention
                return message.mentionedMembers[0]!!.user
            }
            arguments.size > 1 && arguments[1].isNotEmpty() -> {
                return message.guild.members.find { it.id == arguments[1] }?.user
                        ?: if (arguments[1].toLongOrNull() != null) Modolotl.jda.retrieveUserById(arguments[1]).complete() else {
                            message.guild.members.find { it.user.name.equals(arguments[1], ignoreCase = true) }?.user
                                    ?: message.guild.members.find { it.effectiveName.equals(arguments[1], ignoreCase = true) }?.user
                                    ?: run {
                                        message.channel.sendMessage("No User found using the given Argument").queue()
                                        null
                                    }
                        }
            }
            else -> {
                //Missing Argument
                message.channel.sendMessage("Invalid arguments").queue()
            }
        }
    } catch (e: Exception) {
        message.channel.sendMessage("Error occured, please check console").queue()
    }
    return null
}

enum class CommandPermission {
    USER, MOD
}