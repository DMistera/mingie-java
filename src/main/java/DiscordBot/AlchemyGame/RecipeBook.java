package DiscordBot.AlchemyGame;

import DiscordBot.AlchemyGame.Parser.RecipeBookRaw;
import DiscordBot.AlchemyGame.Parser.RecipeRaw;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeBook {

    public RecipeBook(RecipeBookRaw raw) {
        elements = new ArrayList<>();
        recipes = new ArrayList<>();
        for(Map.Entry<Integer, String> e : raw.names.entrySet()) {
            Element element = new Element(e.getKey(), e.getValue());
            elements.add(element);

        }
        for(RecipeRaw recipeRaw : raw.recipes) {
            try {
                Element[] ingredients = new Element[recipeRaw.ingredients.length];
                for (int i = 0; i < recipeRaw.ingredients.length; i++) {
                    ingredients[i] = findElementByID(recipeRaw.ingredients[i]);
                }
                Element[] results = new Element[recipeRaw.results.length];
                for (int i = 0; i < recipeRaw.results.length; i++) {
                    results[i] = findElementByID(recipeRaw.results[i]);
                }
                Recipe recipe = new Recipe(ingredients, results);
                recipes.add(recipe);
            }
            catch (ElementNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Element findElementByID(int id) throws ElementNotFoundException {
        for(Element e : elements) {
            if(e.getId() == id) {
                return e;
            }
        }
        throw new ElementNotFoundException();
    }

    public Element findElementByName(String name) throws ElementNotFoundException {
        for(Element e : elements) {
            if(e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        throw new ElementNotFoundException();
    }

    public List<Element> findPrimaryElements() {
        List<Element> result = new ArrayList<>();

        class ElementContainer {
            Element element;
            boolean taken = false;
        }
        List<ElementContainer> list = new ArrayList<>();
        for(Element element : elements) {
            ElementContainer ec = new ElementContainer();
            ec.element = element;
            list.add(ec);
        }

        for(Recipe recipe : recipes) {
            for(Element resultElement : recipe.getResults()) {
                for(ElementContainer ec : list) {
                    if(resultElement.equals(ec.element)) {
                        ec.taken = true;
                    }
                }
            }
        }

        for(ElementContainer ec : list) {
            if(!ec.taken) {
                result.add(ec.element);
            }
        }

        return result;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    private List<Element> elements;
    private List<Recipe> recipes;
}
