package DiscordBot.AlchemyGame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.List;

public class ImageCreator {

    BufferedImage background;
    Image frame;
    Image plus;
    Image equal;

    Font font;

    public static final int BORDER_WIDTH = 20;
    public static  final int FRAME_SIZE = 120;
    public static  final int SIGN_SIZE = 75;
    public static final int FONT_SIZE = 25;

    public void init() throws IOException, FontFormatException {
        background = ImageIO.read(this.getClass().getResourceAsStream("/alchemyBackground.png"));
        frame = ImageIO.read(this.getClass().getResourceAsStream("/alchemyFrame.png")).getScaledInstance(FRAME_SIZE, FRAME_SIZE, BufferedImage.SCALE_SMOOTH);
        plus = ImageIO.read(this.getClass().getResourceAsStream("/alchemyPlus.png")).getScaledInstance(SIGN_SIZE, SIGN_SIZE, BufferedImage.SCALE_SMOOTH);
        equal = ImageIO.read(this.getClass().getResourceAsStream("/alchemyEqual.png")).getScaledInstance(SIGN_SIZE, SIGN_SIZE, BufferedImage.SCALE_SMOOTH);
        font = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/DOMINICA.TTF"));
    }

    public BufferedImage createRecipeImage(Recipe recipe, List<Element> unlcokedElements) {


        int frameCount = 0;
        for(Element e : recipe.getIngredients()) {
            frameCount++;
        }
        for(Element e : recipe.getResults()) {
            frameCount++;
        }

        int frameIntervalSize = SIGN_SIZE + 20;
        int width = BORDER_WIDTH*2 + frameCount*FRAME_SIZE + (frameCount - 1)*frameIntervalSize;
        int height = 400;

        int recipeWidth = width - 2* BORDER_WIDTH;

        CreateBaseImageResult r = createBaseImage(width, height);
        Graphics2D graphics2D = r.graphics2D;
        BufferedImage result = r.bufferedImage;

        Point pos = new Point(BORDER_WIDTH + FRAME_SIZE /2, height/2);

        drawElements(graphics2D, recipe.getIngredients(), pos, frameIntervalSize);

        pos.x += frameIntervalSize/2;
        drawImageCentered(graphics2D, equal, pos);
        pos.x += frameIntervalSize/2 + FRAME_SIZE /2;

        drawElements(graphics2D, recipe.getResults(), pos, frameIntervalSize);

        if(unlcokedElements.size() > 0) {
            String footer = "You have obtained " + unlcokedElements.get(0);
            for(int i = 1; i < unlcokedElements.size(); i++) {
                footer += " and " + unlcokedElements.get(i);
            }
            setFontSize(graphics2D, FONT_SIZE);
            drawStringCentered(graphics2D, footer, new Point(width/2, height - FONT_SIZE*2));
        }

        graphics2D.dispose();
        return result;
    }

    private void drawImageCentered(Graphics2D graphics2D, Image img, Point pos) {
        graphics2D.drawImage(img, pos.x - img.getWidth(null)/2, pos.y - img.getHeight(null)/2, null);
    }

    private void drawStringCentered(Graphics2D graphics2D, String string, Point pos) {
        int width = graphics2D.getFontMetrics().stringWidth(string);
        int height = graphics2D.getFont().getSize();
        graphics2D.drawString(string, pos.x - width/2, pos.y - height/2);
    }

    private void drawElement(Graphics2D graphics2D, Element e, Point pos) {
        drawImageCentered(graphics2D, e.getImage(), pos);
        drawImageCentered(graphics2D, frame, pos);

        String name = e.getName();

        setFontSize(graphics2D, name, FRAME_SIZE, FONT_SIZE);
        int fontSize = graphics2D.getFont().getSize();
        int width = graphics2D.getFontMetrics().stringWidth(name);
        graphics2D.drawString(name, pos.x - width/2, pos.y + FRAME_SIZE/2 + fontSize);
    }

    private void drawElements(Graphics2D graphics2D, Element[] elements, Point pos, int frameIntervalSize) {
        drawElement(graphics2D, elements[0], pos);
        pos.x += FRAME_SIZE /2;

        for(int i = 1; i < elements.length; i++) {
            pos.x += frameIntervalSize/2;
            drawImageCentered(graphics2D, plus, pos);
            pos.x += frameIntervalSize/2 + FRAME_SIZE /2;
            drawElement(graphics2D, elements[i], pos);
            pos.x += FRAME_SIZE /2;
        }
    }

    private void setFontSize(Graphics2D g, String str, int desiredWidth, float maximum) {
        int currentWidth = g.getFontMetrics().stringWidth(str);
        float ratio = (float)desiredWidth/(float)currentWidth;
        Font currFont = g.getFont();
        float newFontSize = ratio*currFont.getSize();
        if(newFontSize > maximum) {
            newFontSize = maximum;
        }
        g.setFont(currFont.deriveFont(newFontSize));
    }

    private void setFontSize(Graphics2D g, float size) {
        Font currFont = g.getFont();
        g.setFont(currFont.deriveFont(size));
    }

    class CreateBaseImageResult {
        BufferedImage bufferedImage;
        Graphics2D graphics2D;
    }

    private final float backgroundRatio = 0.625f;

    private CreateBaseImageResult createBaseImage(int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics2D =  bufferedImage.createGraphics();

        float ratio = (float)height/(float)width;
        if(ratio > backgroundRatio) {
            width = (int)((float)height*(1.0f/backgroundRatio));
        }
        else {
            height = (int)(backgroundRatio*width);
        }
        Image backgroundScaled = background.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
        graphics2D.drawImage(backgroundScaled, 0, 0, null);

        graphics2D.setFont(font);
        graphics2D.setColor(Color.WHITE);

        CreateBaseImageResult result = new CreateBaseImageResult();
        result.bufferedImage = bufferedImage;
        result.graphics2D = graphics2D;

        return result;
    }

    public BufferedImage createInventoryImage(Player player, RecipeBook recipeBook) {
        List<PlayerElement> elements = player.getPlayerElements();
        int headerHeight = FONT_SIZE*2;
        String header = player.getUser().getName() + " has unlocked " + elements.size() + " out of " + recipeBook.getElements().size() + " elements!";
        int framesInRow = (int)Math.ceil(Math.sqrt(elements.size()/backgroundRatio));
        int height = (int)Math.ceil((float)elements.size() / (float)framesInRow);
        int intervalWidth = 25;
        int imageWidth = BORDER_WIDTH*2 + framesInRow*FRAME_SIZE + intervalWidth*(framesInRow - 1);
        int intervalHeight = intervalWidth + FONT_SIZE;
        int imageHeight = BORDER_WIDTH*2 + height*(FRAME_SIZE + FONT_SIZE) + intervalWidth*(height - 1) + headerHeight;
        CreateBaseImageResult r = createBaseImage(imageWidth, imageHeight);
        Graphics2D graphics2D = r.graphics2D;
        BufferedImage result = r.bufferedImage;

        setFontSize(graphics2D, FONT_SIZE);
        drawStringCentered(graphics2D, header,new Point(imageWidth/2, BORDER_WIDTH + headerHeight/2));

        Point position = new Point(BORDER_WIDTH + FRAME_SIZE/2, BORDER_WIDTH + FRAME_SIZE/2 + headerHeight);
        for(int y = 0; y < height; y++) {
            position.x = BORDER_WIDTH + FRAME_SIZE/2;
            for(int x = 0; x < framesInRow; x++) {
                int index = x + framesInRow*y;
                if(index < elements.size()) {
                    drawElement(graphics2D, elements.get(index).getType(), position);
                }
                else {
                    break;
                }
                position.x += FRAME_SIZE + intervalWidth;
            }
            position.y += FRAME_SIZE + intervalHeight;
        }
        return result;
    }
}
