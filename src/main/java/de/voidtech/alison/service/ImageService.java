package main.java.de.voidtech.alison.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 800;
    private static final int OVERLAY_X = 20;
    private static final int OVERLAY_Y = 565;
    private static final int OVERLAY_WIDTH = 760;
    private static final int OVERLAY_HEIGHT = 210;
    private static final int MAX_TEXT_WIDTH = 740;
    private static final int QUOTE_Y = 585;
    private static final int USERNAME_Y = 715;
    private static final int LINE_HEIGHT = 43;

    public byte[] createQuoteImage(String avatarUrl, String username, String quote) throws IOException {
        BufferedImage canvas = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ctx = canvas.createGraphics();

        ctx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ctx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ctx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        BufferedImage backgroundImage = loadImage(avatarUrl + "?size=2048");
        ctx.drawImage(backgroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, null);

        ctx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        ctx.setColor(Color.BLACK);
        ctx.fillRect(OVERLAY_X, OVERLAY_Y, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        ctx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        String formattedQuote = "\"" + quote + "\"";
        String formattedUsername = "-" + username;

        ctx.setFont(loadCustomFont("/fonts/whitneylight.otf", 40));
        List<String> lines = getLines(ctx, formattedQuote, MAX_TEXT_WIDTH);
        ctx.setColor(Color.WHITE);

        int offset = 0;
        for (String line : lines) {
            drawCenteredText(ctx, line, CANVAS_WIDTH / 2, QUOTE_Y + offset);
            offset += LINE_HEIGHT;
        }

        setTextSize(ctx, formattedUsername, 300);
        drawCenteredText(ctx, formattedUsername, CANVAS_WIDTH / 2, USERNAME_Y);

        ctx.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(canvas, "png", baos);
        return baos.toByteArray();
    }

    private BufferedImage loadImage(String urlString) throws IOException {
        URL url = new URL(urlString);
        return ImageIO.read(url);
    }

    private void setTextSize(Graphics2D ctx, String text, int maxWidth) {
        int size = 30;
        Font font = loadCustomFont("/fonts/whitneylight.otf", size);
        ctx.setFont(font);

        FontMetrics metrics = ctx.getFontMetrics();
        while (metrics.stringWidth(text) > maxWidth && size > 8) {
            size--;
            font = new Font(Font.SANS_SERIF, Font.BOLD, size);
            ctx.setFont(font);
            metrics = ctx.getFontMetrics();
        }
    }

    private List<String> getLines(Graphics2D ctx, String text, int maxWidth) {
        String[] words = text.split(" ");
        List<String> lines = new ArrayList<>();

        if (words.length == 0) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder(words[0]);
        FontMetrics metrics = ctx.getFontMetrics();

        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            String testLine = currentLine + " " + word;
            int width = metrics.stringWidth(testLine);

            if (width < maxWidth) {
                currentLine.append(" ").append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        lines.add(currentLine.toString());

        return lines;
    }

    private void drawCenteredText(Graphics2D ctx, String text, int x, int y) {
        FontMetrics metrics = ctx.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int adjustedX = x - (textWidth / 2);
        ctx.drawString(text, adjustedX, y + metrics.getAscent());
    }

    private Font loadCustomFont(String fontPath, float size) {
        try (InputStream is = getClass().getResourceAsStream(fontPath)) {

            if (is == null) {
                throw new IllegalStateException("Font not found at path: " + fontPath);
            }

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            return baseFont.deriveFont(Font.BOLD, size);

        } catch (Exception e) {
            e.printStackTrace();
            return new Font(Font.SANS_SERIF, Font.BOLD, (int) size);
        }
    }

}
