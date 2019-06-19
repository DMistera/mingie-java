package DiscordBot;

import net.dv8tion.jda.core.entities.Message;

public interface CommandListener {
     void onCommand(Message msg, String command, String[] arguments);
}
