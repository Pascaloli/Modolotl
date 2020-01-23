package me.pascal.modolotl.utils

import kotlinx.serialization.Serializable
import me.pascal.modolotl.command.Command
import me.pascal.modolotl.command.CommandHandler

/**
 * Data class that contains the Discord Bot [token], the [modRoleId] as well as the command [prefix].
 *
 * @property token The Discord Bot token used to authorize against the Discord gateway.
 * @property modRoleId The snowflake ID of the moderator role that is used to check if a member can use a [Command] that requires moderator permission.
 * @property logChannelId The snowflake ID of the Channel to log events into
 * @property prefix The command prefix used in the [CommandHandler] to check if a message is a command.
 * @author NurMarvin
 */
@Serializable
data class Settings(var token: String = "", var modRoleId: String = "", var mutedRoleId: String = "", var logChannelId: String = "", var prefix: String = "")