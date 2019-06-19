package DiscordBot.AlchemyGame.Parser;

import DiscordBot.MyUtils;
import com.google.gson.Gson;
import java.io.InputStream;

public class RecipeParser {

    public RecipeBookRaw parse() {
        InputStream stream = this.getClass().getResourceAsStream("/recipes.json");
        String s = MyUtils.inputStreamToString(stream);
        Gson gson = new Gson();
        RecipeBookRaw book = gson.fromJson(s, RecipeBookRaw.class);
        return book;
    }
}
