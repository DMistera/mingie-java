package DiscordBot;

import DiscordBot.AlchemyGame.AlchemyGame;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Bot implements EventListener {

    private final String myMasterUserID = "150994488515362817";

    public final String prefix = "!";

    private JDA jda;
    private User myMaster;

    public static Random RANDOM = new Random();

    private AlchemyGame alchemyGame;

    public Bot() {
        commandListeners = new ArrayList<>();
        masterListeners = new ArrayList<>();
    }

    public void start() {
        try {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "dpwamrapj",
                    "api_key", "214858829896364",
                    "api_secret", "rCFT9Jbs4v6eb6D97BQi2ATV_Tk"));

            alchemyGame = new AlchemyGame(this);
            jda = new JDABuilder(System.getenv("mainToken")).addEventListener(this).build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    private List<CommandListener> commandListeners;
    private List<MasterListener> masterListeners;

    @Override
    public void onEvent(Event event) {
        if(event instanceof ReadyEvent) {
            System.out.println("Bot is ready to go!");
            try {
                myMaster = jda.getUserById(myMasterUserID);
                alchemyGame.init();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else  if(event instanceof MessageReceivedEvent) {
            Message msg = ((MessageReceivedEvent) event).getMessage();
            if(msg.getAuthor().getId().equals(myMasterUserID)) {
                for(MasterListener masterListener : masterListeners) {
                    masterListener.onMasterMessage(msg);
                }
            }
            String content = msg.getContentDisplay();
            if(content.startsWith(prefix)) {
                String commandBody = content.substring(prefix.length());
                String[] split = commandBody.split(" ");
                String command = split[0];
                String[] arguments;
                if(split.length > 1) {
                    arguments = Arrays.copyOfRange(split, 1, split.length);
                }
                else {
                    arguments = new String[0];
                }
                for(CommandListener commandListener : commandListeners) {
                    commandListener.onCommand(msg, command, arguments);
                }
            }
        }
    }

    public void addCommandListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
    }

    public void addMasterListener(MasterListener masterListener) {
        masterListeners.add(masterListener);
    }

    public void sendMessage(TextChannel channel, String content) {
        sendMessage(channel, content, null);
    }

    public void sendMessage(TextChannel channel, String content, Consumer<Message> callback) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(content);
        channel.sendMessage(builder.build()).queue(callback);
    }

    public void sendMessageAndDelete(TextChannel channel, String content, long timeout) {
        sendMessage(channel, content, message -> message.delete().queueAfter(timeout, TimeUnit.MILLISECONDS) );
    }

    Cloudinary cloudinary;

    public void sendImage(TextChannel channel, BufferedImage image) {
        sendImage(channel, image, null);
    }

    public void sendImage(TextChannel channel, BufferedImage image, Consumer<Message> callback) {
        try {
            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", boas);
            boas.flush();
            Map uploadResult = cloudinary.uploader().upload(boas.toByteArray(), ObjectUtils.emptyMap());
            boas.close();
            String url = (String)uploadResult.get("url");
            EmbedBuilder eb = new EmbedBuilder();
            eb.setImage(url);
            MessageEmbed embed = eb.build();
            channel.sendMessage(embed).queue(message -> {
                /*try {
                    cloudinary.uploader().destroy((String)uploadResult.get("public_id"), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                if(callback != null) {
                    callback.accept(message);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendImageAndDelete(TextChannel channel, BufferedImage image, long timeout) {
        sendImage(channel, image, message -> message.delete().queueAfter(timeout, TimeUnit.MILLISECONDS));
    }

    public void sendMessageToMaster(String message) {
        if(message.length() > 0) {
            myMaster.openPrivateChannel().queue((channel) -> {
                channel.sendMessage(message).queue();
            });
        }
    }

    public JDA getJDA() {
        return jda;
    }


}
