package DiscordBot.AlchemyGame.Parser;


import DiscordBot.AlchemyGame.Parser.RecipeBookRaw;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class RecipeParser {

    public RecipeBookRaw parse() throws FileNotFoundException {
        FileReader reader = new FileReader(this.getClass().getResource("/recipes.json").getPath());
        Gson gson = new Gson();
        RecipeBookRaw book = gson.fromJson(reader, RecipeBookRaw.class);
        return book;
    }
}
