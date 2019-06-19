package DiscordBot;

import net.dv8tion.jda.core.entities.Message;

public interface MasterListener {
    void onMasterMessage(Message msg);
}
