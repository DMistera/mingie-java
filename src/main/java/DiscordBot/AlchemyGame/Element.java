package DiscordBot.AlchemyGame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Element {

    public Element(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String name;
    private int id;

    public Image getImage() {
        return image;
    }

    public void setImageFromURL(String urlString) throws IOException {
        int size = ImageCreator.FRAME_SIZE;
        imageURL = urlString;
        if(urlString == null) {
            image = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);
        }
        else {
            URL url = new URL(urlString);
            image = ImageIO.read(url).getScaledInstance(size, size, BufferedImage.SCALE_SMOOTH);
        }
    }

    private Image image;

    public String getImageURL() {
        return imageURL;
    }

    private String imageURL;

    @Override
    public boolean equals(Object e) {
        if(e instanceof Element) {
            return getId() == ((Element)e).getId();
        }
        else {
            return super.equals(e);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
