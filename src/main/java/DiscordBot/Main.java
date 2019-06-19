package DiscordBot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.List;

public class Main implements EventListener {

    static  JDA jda;

    public static  void main(String[] a) {
        Bot bot = new Bot();
        bot.start();
    }

    TextChannel privateChannel;
    TextChannel targetChannel;

    @Override
    public void onEvent(Event event)
    {
        String targetChannelID = "483933623096049677";
        String privateChannelId =  "504382196350582784"  ;

        if (event instanceof ReadyEvent) {
            System.out.println("API is ready!");

            List<TextChannel> tl = jda.getTextChannels();
            for(TextChannel t : tl) {
                System.out.println(t.getGuild().getName() + ":-- " + t.getName() + " :-- " + t.getId() + ":--" + t.getGuild().getMembers().size());
            }
            privateChannel = jda.getTextChannelById(privateChannelId);
            targetChannel = jda.getTextChannelById(targetChannelID);
        }
        else if(event instanceof MessageReceivedEvent) {
            Message msg =((MessageReceivedEvent) event).getMessage();
            if(msg.getChannel().getId().equals(targetChannelID)) {
                String content = msg.getAuthor().getName() + ": " + msg.getContentDisplay();
                privateChannel.sendMessage(content).queue();
            }
            else if(msg.getChannel().getId().equals(privateChannelId)) {
                if(!msg.getAuthor().isBot()) {
                    targetChannel.sendMessage(msg.getContentDisplay()).queue();
                }
            }
        }
    }



}
