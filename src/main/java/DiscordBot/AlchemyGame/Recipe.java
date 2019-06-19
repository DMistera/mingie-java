package DiscordBot.AlchemyGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Recipe {
    public Element[] getIngredients() {
        return ingredients;
    }

    public void setIngredients(Element[] ingredients) {
        this.ingredients = ingredients;
    }

    public Element[] getResults() {
        return results;
    }

    public void setResults(Element[] results) {
        this.results = results;
    }

    public Recipe(Element[] ingredients, Element[] results) {
        this.ingredients = ingredients;
        this.results = results;
    }

    @Override
    public String toString() {
        String result = "";
        result += ingredients[0].getName();
        for(int i = 1; i < ingredients.length; i++) {
            result += " + " + ingredients[i].getName();
        }
        result += " = " + results[0].getName();
        for(int i = 1; i < results.length; i++) {
            result += " + " + results[i].getName();
        }
        return result;
    }

    public boolean isMatch(Element[] ingredients) {

        Map<Element, Integer> ingredientsMap = new HashMap<>();
        for(Element e : ingredients) {
            if(ingredientsMap.containsKey(e)) {
                int value = ingredientsMap.get(e);
                ingredientsMap.put(e, value + 1);
            }
            else {
                ingredientsMap.put(e, 1);
            }
        }
        Map<Element, Integer> ingredientsMap2 = new HashMap<>();
        for(Element e : this.ingredients) {
            if(ingredientsMap2.containsKey(e)) {
                int value = ingredientsMap2.get(e);
                ingredientsMap2.put(e, value + 1);
            }
            else {
                ingredientsMap2.put(e, 1);
            }
        }
        return ingredientsMap.equals(ingredientsMap2);
    }

    private Element[] ingredients;
    private Element[] results;
}
