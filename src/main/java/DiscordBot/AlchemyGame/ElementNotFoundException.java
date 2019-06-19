package DiscordBot.AlchemyGame;

public class ElementNotFoundException extends Exception {

    @Override
    public String getMessage() {
        return "Element does not exist!";
    }
}
