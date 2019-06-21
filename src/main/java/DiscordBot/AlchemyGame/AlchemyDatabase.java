package DiscordBot.AlchemyGame;

import DiscordBot.Bot;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class AlchemyDatabase {

    //private final String url = "jdbc:h2:C:/DB/Discord-Database";

    private final String serverUrl = "jdbc:mysql://remotemysql.com:3306/K297MoGjlA?autoReconnect=true";
    private final String username = "K297MoGjlA";

    private Connection connection;

    public void connect() throws SQLException {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(serverUrl, username, System.getenv("databasePassword"));

            if (connection!= null){
                System.out.println("Connection created successfully");
            }else{
                System.out.println("Problem with creating connection");
            }

        }  catch (ClassNotFoundException e) {
            e.printStackTrace(System.out);
        }
    }



    public List<PlayerElement> getPlayerElements(Player player, RecipeBook book) {
        List<PlayerElement> r = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("select elementID from PLAYER_ELEMENTS where USERID = " + player.getUser().getId());
            while (result.next()) {
                r.add(new PlayerElement(book.findElementByID(result.getInt("elementID"))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
        return r;
    }

    public void addPlayerElement(Player player, PlayerElement playerElement) {
        try {
            Statement statement = connection.createStatement();
            String uID = player.getUser().getId();
            int eId = playerElement.getType().getId();
            statement.execute("insert into PLAYER_ELEMENTS values (" + uID + "," + eId + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Player> getPlayers(Bot bot, RecipeBook book) {
        List<Player> r = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("select * from PLAYERS");
            while(result.next()) {
                String userID = result.getString("userID");
                User user = bot.getJDA().getUserById(userID);
                long lastTipTime = result.getLong("lastTipTime");
                Player player = new Player(user, lastTipTime);
                player.setPlayerElements(getPlayerElements(player, book));
                r.add(player);
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return r;
    }

    private void executeAsync(String sql) {
        new Thread(() -> {
            try {
                Statement statement = connection.createStatement();
                statement.execute(sql);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void addPlayer(Player p) {
        executeAsync("insert into PLAYERS values (" + p.getUser().getId() + "," + p.getLastTipTime() + ")");
    }

    public void addSuggestion(ElementImageSuggestion suggestion) {
        executeAsync("insert into IMAGE_SUGGESTIONS values (" + suggestion.getAuthor().getId() + "," + suggestion.getElement().getId() +  ",'" + suggestion.getSuggestedImageURL() + "');");
    }

    public void deleteSuggestion(ElementImageSuggestion suggestion) {
        executeAsync("delete from IMAGE_SUGGESTIONS where AUTHORID = " + suggestion.getAuthor().getId() + " and ELEMENTID = " + suggestion.getElement().getId() + ";");
    }

    public void applySuggestion(ElementImageSuggestion suggestion) {
        int elementID = suggestion.getElement().getId();
        executeAsync("update ELEMENTS set IMAGEURL = '" + suggestion.getSuggestedImageURL() + "' where ELEMENTID = " + elementID + ";");
        executeAsync("insert into ELEMENTS select " + elementID + ",'" + suggestion.getSuggestedImageURL() + "' where not exists (select * from ELEMENTS where ELEMENTID =" + elementID + ")");
        deleteSuggestion(suggestion);
    }

    public List<ElementImageSuggestion> getSuggestions(Bot bot, RecipeBook book) {
        List<ElementImageSuggestion> r = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("select * from IMAGE_SUGGESTIONS");
            while(result.next()) {
                String userID = result.getString("authorID");
                User author = bot.getJDA().getUserById(userID);
                int elementID = result.getInt("elementID");
                Element element = book.findElementByID(elementID);
                String imageURL = result.getString("imageURL");
                ElementImageSuggestion suggestion = new ElementImageSuggestion(author, element, imageURL);
                r.add(suggestion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
        return r;
    }

    public String getElementImageURL(Element e) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("select * from ELEMENTS where ELEMENTID = " + e.getId());
            if(result.next()) {
                return result.getString("ImageURL");
            }
            else {
                return null;
            }
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void loadElementsURL(RecipeBook book) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("select * from ELEMENTS;");
            while(result.next()) {
                String imageURL = result.getString("ImageURL");
                Element element = book.findElementByID(result.getInt("elementID"));
                element.setImageFromURL(imageURL);
            }
            for(Element element : book.getElements()) {
                if(element.getImageURL() == null) {
                    element.setImageFromURL(null);
                }
            }
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
    }
}
