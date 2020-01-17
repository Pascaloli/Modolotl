package me.pascal.modolotl.command.commands

import me.pascal.modolotl.command.Command
import me.pascal.modolotl.command.PERMISSIONS
import net.dv8tion.jda.api.entities.Message

class MuteCommand : Command("mute", PERMISSIONS.MOD) {

    override fun handle(message: Message) {
        //TODO

        super.handle(message)
    }

}