package DiscordBot.AlchemyGame;

import net.dv8tion.jda.core.entities.User;

import java.io.IOException;

public class ElementImageSuggestion {


    private User author;
    private Element element;

    public ElementImageSuggestion(User author, Element element, String suggestedImageURL) {
        this.author = author;
        this.element = element;
        this.suggestedImageURL = suggestedImageURL;
    }

    private String suggestedImageURL;

    public User getAuthor() {
        return author;
    }

    public Element getElement() {
        return element;
    }

    public String getSuggestedImageURL() {
        return suggestedImageURL;
    }

    @Override
    public String toString() {
        String result = author.getName() + " suggested as new image for " + element.getName() + ".\n";
        result += "Old image: " + element.getImageURL() + "\n";
        result += "New image: " + suggestedImageURL + "\n";
        return  result;
    }

    public void apply(AlchemyDatabase database) throws IOException {
        element.setImageFromURL(suggestedImageURL);
        database.applySuggestion(this);
        String msg = "Your suggestion has been accepted:\n" + this;
        getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(msg).queue());
    }

    public void reject(AlchemyDatabase database) {
        database.deleteSuggestion(this);
        String msg = "Your suggestion has been rejected:\n" + this;
        getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(msg).queue());
    }
}
