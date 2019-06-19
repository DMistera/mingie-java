package DiscordBot.AlchemyGame;

import DiscordBot.Bot;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String[] startingElements = {"air", "water", "fire", "earth"};

    public Player(User user, long lastTipTime) {
        this.user = user;
        playerElements = new ArrayList<>();
        this.lastTipTime = lastTipTime;
    }

    public  Player(User user) {
        this(user, 0);
    }

    public void start(RecipeBook book, AlchemyDatabase database) {
        database.addPlayer(this);
        for(String s : startingElements) {
            try {
                Element e = book.findElementByName(s);
                addElement(e, database);
            } catch (ElementNotFoundException e1) {
                System.err.println("ERROR! Cannot generae starter pack because element was not found!");
            }
        }
    }

    public User getUser() {
        return user;
    }



    private User user;

    public long getLastTipTime() {
        return lastTipTime;
    }

    private long lastTipTime;

    public void setPlayerElements(List<PlayerElement> playerElements) {
        this.playerElements = playerElements;
    }

    public List<PlayerElement> getPlayerElements() {
        return playerElements;
    }

    private List<PlayerElement> playerElements;

    public boolean isUser(User user) {
        return user.getId().equals(this.user.getId());
    }

    public boolean hasElement(Element e) {
        for(PlayerElement playerElement : playerElements) {
            if(playerElement.getType().equals(e)) {
                return true;
            }
        }
        return false;
    }

    final long tipCooldownTime = 15000;

    public String getTip(RecipeBook recipeBook) {
        long diff = System.currentTimeMillis() - lastTipTime;
        if(diff < tipCooldownTime) {
            return "I won't tell you everything about alchemy. You need to figure some things on your own or just wait " + (tipCooldownTime - diff)/1000 + " seconds";
        }
        else {
            Element e = getAvailableElement(recipeBook);
            if (e == null) {
                return "You have unlocked all playerElements! Congratulaations!\n";
            } else {
                lastTipTime = System.currentTimeMillis();
                return "Try to create " + e.toString() + "!\n";
            }
        }
    }

    public Element getAvailableElement(RecipeBook recipeBook) {
        for(Recipe recipe : recipeBook.getRecipes()) {
            if(isRecipeAvailable(recipe)) {
                for(Element result : recipe.getResults()) {
                    if(!hasElement(result)) {
                        return  result;
                    }
                }
            }
        }
        return null;
    }

    public boolean isRecipeAvailable(Recipe recipe) {
        for(Element ingredient : recipe.getIngredients()) {
            if(!hasElement(ingredient)) {
                return  false;
            }
        }
        for(Element result : recipe.getResults()) {
            if(!hasElement(result)) {
                return true;
            }
        }
        return false;
    }

    public String getPlayerElementsString() {
        String result = user.getName() + " unlocked " + playerElements.size() +" playerElements:\n";
        for(PlayerElement pe : playerElements) {
            result += pe.getType() + "\n";
        }
        return result;
    }

    public Recipe findRecipe(RecipeBook book, Element[] ingredients) {
        for(Recipe recipe : book.getRecipes()) {
            if(recipe.isMatch(ingredients)) {
                return recipe;
            }
        }
        return null;
    }

    public void addElement(Element e, AlchemyDatabase database) {
        PlayerElement playerElement = new PlayerElement(e);
        playerElements.add(playerElement);
        database.addPlayerElement(this, playerElement);
    }

    class CombineResult {
        boolean success;
        String message;
        Recipe recipe;
        List<Element> unlockedElements;
    }

    private String[] failMessages = {
            "You mixed the elements but nothing came out!",
            "Nothing happened!",
            "Unfortunately, you've only created disappointment.",
            "Mix, mix, mix... and it's nothing.",
            "Wait, What? What did you expect to achieve combining these?",
            "The elements combined gracefully into a pile of rubbish.",
            "Maybe try something else?",
            "And you call yourself and alchemist?",
            "Im disappointed.",
            "Come on, just think of something better",
            "You failed miserably",
            "Try something else!",
            "Why did you think it was a good idea?",
            "You created a pile of garbage. Now I have to throw it out. By myself.",
            "Eh... are you sure?",
            "Just... why? Why these? Is there any reason?",
            "Maybe you should reconsider your job as an alchemist.",
            "Nope, that won't work.",
            "Stop wasting materials!"
    };

    public CombineResult combine(RecipeBook book, Element[] ingredients, AlchemyDatabase database) {
        CombineResult result = new CombineResult();
        String message;
        for(Element e : ingredients) {
            if(!hasElement(e)) {
                result.success = false;
                result.message = "You dont have " + e + "!";
                return result;
            }
        }

        Recipe recipe = findRecipe(book, ingredients);
        if(recipe == null) {
            result.success = false;
            result.message = failMessages[Bot.RANDOM.nextInt(failMessages.length - 1)];
        }
        else {
            result.success = true;
            result.recipe = recipe;
            result.unlockedElements = new ArrayList<>();
            for(Element e : recipe.getResults()) {
                if(!hasElement(e)) {
                    addElement(e, database);
                    result.unlockedElements.add(e);
                }
            }
        }
        return result;
    }
}
