package DiscordBot.AlchemyGame;

public class PlayerElement {
    public Element getType() {
        return type;
    }

    public void setType(Element type) {
        this.type = type;
    }

    public PlayerElement(Element type) {
        this.type = type;
    }

    private Element type;

}
