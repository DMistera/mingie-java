package DiscordBot.AlchemyGame;

import DiscordBot.AlchemyGame.Parser.RecipeBookRaw;
import DiscordBot.AlchemyGame.Parser.RecipeParser;
import DiscordBot.Bot;
import DiscordBot.CommandListener;
import DiscordBot.MasterListener;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

public class AlchemyGame implements CommandListener, MasterListener {

    List<Player> players;
    RecipeBook recipeBook;
    AlchemyDatabase database;
    ImageCreator imageCreator;
    String helpMessage;

    List<ElementImageSuggestion> suggestions;

    private Bot bot;


    public AlchemyGame(Bot bot) {
        bot.addCommandListener(this);
        bot.addMasterListener(this);
        this.bot = bot;
        players = new ArrayList<>();
        suggestions = new ArrayList<>();
    }

    public void init() throws IOException, SQLException, FontFormatException {

        database = new AlchemyDatabase();
        database.connect();

        RecipeParser parser = new RecipeParser();
        RecipeBookRaw bookRaw = parser.parse();
        recipeBook = new RecipeBook(bookRaw);
        database.loadElementsURL(recipeBook);
        System.out.println("Recipe book has been prepared!");

        players = database.getPlayers(bot, recipeBook);
        System.out.println("Players have been loaded!");
        suggestions = database.getSuggestions(bot, recipeBook);
        System.out.println("Suggestions have been loaded!");

        imageCreator = new ImageCreator();
        imageCreator.init();

        helpMessage = "";
        File helpFile = new File( this.getClass().getResource("/alchemyHelp.txt").getPath());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(helpFile));
        String line;
        while((line = bufferedReader.readLine()) != null) {
            helpMessage += line + "\n";
        }

        System.out.println("Alchemy game is ready to go!");
    }


    @Override
    public void onCommand(Message msg, String command, String[] arguments) {
        if(command.equals("ping")) {
            bot.sendMessage(msg.getTextChannel(), "pong");
        }
        else if(command.equals("me")) {
            BufferedImage img = imageCreator.createInventoryImage(getPlayer(msg.getAuthor()), recipeBook);
            bot.sendImage(msg.getTextChannel(), img);
        }
        else if(command.equals("combine") || command.equals("c")) {
            try {
                String formula = String.join(" ", arguments);
                String[] split = formula.split("\\+");

                Element[] ingredients = new Element[split.length];
                for (int i = 0; i < split.length; i++) {
                    ingredients[i] = recipeBook.findElementByName(split[i].trim());
                }
                Player.CombineResult combineResult = getPlayer(msg.getAuthor()).combine(recipeBook, ingredients, database);
                if(combineResult.success) {
                    bot.sendImageAndDelete(msg.getTextChannel(), imageCreator.createRecipeImage(combineResult.recipe, combineResult.unlockedElements), 10000);
                }
                else {
                    bot.sendMessageAndDelete(msg.getTextChannel(), combineResult.message, 10000);
                }
            }
            catch (ElementNotFoundException e) {
                bot.sendMessage(msg.getTextChannel(), "You dont have that element!");
            }
        }
        else if(command.equals("tip")) {
            bot.sendMessage(msg.getTextChannel(), getPlayer(msg.getAuthor()).getTip(recipeBook));
        }
        else if(command.equals("suggest")) {
            if(arguments.length >= 2) {
                try {
                    String elementName = String.join(" ", Arrays.copyOfRange(arguments, 0, arguments.length - 1));
                    System.out.println(elementName);
                    Element element = recipeBook.findElementByName(elementName);
                    ElementImageSuggestion suggestion = new ElementImageSuggestion(msg.getAuthor(), element, arguments[1]);
                    addSuggestion(suggestion);
                    bot.sendMessageToMaster(msg.getAuthor().getName() + " has sent a new suggestion: \n" + suggestion);
                    bot.sendMessage(msg.getTextChannel(), "Your suggestion have been sent! It should be reviewed in 24 hours!");
                }
                catch (ElementNotFoundException e) {
                    bot.sendMessage(msg.getTextChannel(), "Given element does not exist!");
                }
            }
            else {
                bot.sendMessage(msg.getTextChannel(), "Invalid number of arguments!");
            }
        }
        else  if(command.equals("help")) {
            bot.sendMessage(msg.getTextChannel(), helpMessage);
        }
    }

    public void addSuggestion(ElementImageSuggestion suggestion) {
        suggestions.add(suggestion);
        database.addSuggestion(suggestion);
    }


    private Player getPlayer(User user) {
        for(Player player : players) {
            if(player.isUser(user)) {
                return player;
            }
        }
        Player player = new Player(user);
        player.start(recipeBook, database);
        players.add(player);
        return player;
    }



    @Override
    public void onMasterMessage(Message msg) {
        String content = msg.getContentDisplay();
        String[] split = content.split(" ");
        if(content.equals("view suggestions")) {
            String r = "";
            for(int i = 0 ; i < suggestions.size(); i++) {
                ElementImageSuggestion suggestion = suggestions.get(i);
                r += i + ". " + suggestion;
            }
            if(suggestions.size() == 0) {
                r = "No suggestions :(";
            }
            bot.sendMessageToMaster(r);
        }
        else if(content.equals("accept all")) {
            for (ElementImageSuggestion suggestion : suggestions) {
                try {
                    suggestion.apply(database);
                }
                catch(IOException e) {
                    bot.sendMessageToMaster("This URL is invalid!");
                }
            }
            suggestions.clear();
            bot.sendMessageToMaster("All suggestions accepted!");
        }
        else if(content.startsWith("accept")) {
            try {
                int index = Integer.parseInt(split[1]);
                suggestions.get(index).apply(database);
                suggestions.remove(index);
                bot.sendMessageToMaster("Suggestion accepted!");
            } catch(IndexOutOfBoundsException e) {
                bot.sendMessageToMaster("Invalid index!");
            } catch (IOException e) {
                bot.sendMessageToMaster("This URL is invalid!");
            } catch (NumberFormatException e) {
                bot.sendMessageToMaster("Number format is invalid!");
            }
        }
        else if(content.startsWith("reject")) {
            try {
                int index = Integer.parseInt(split[1]);
                suggestions.get(index).reject(database);
                suggestions.remove(index);
                bot.sendMessageToMaster("Suggestion rejected!");
            } catch(IndexOutOfBoundsException e) {
                bot.sendMessageToMaster("Invalid index!");
            } catch (NumberFormatException e) {
                bot.sendMessageToMaster("Number format is invalid!");
            }
        }
    }
}
