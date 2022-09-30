package vavi.speech.nicotalk;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import vavi.util.Debug;
import vavix.util.ResourceList;


/**
 *
 */
public class Character {
    static final int W = 500;
    static final int H = 500;

    String name;
    enum Part {他, 体, 全, 口, 目, 後, 眉, 顔, 髪};
    Map<String, BufferedImage> images = new HashMap<>();

    Character(String name) throws IOException {
        this.name = name;
        Arrays.stream(Part.values()).forEach(e -> {
            Pattern pattern = Pattern.compile("^.*\\/nicotalk\\/" + name + "\\/(" + e.name() + "\\/.*)\\.png$");
            Collection<String> resources = ResourceList.getResources(pattern);
            resources.forEach(r -> {
                try {
                    BufferedImage image = ImageIO.read(URI.create(r).toURL().openStream());
                    Matcher matcher = pattern.matcher(r);
                    if (matcher.find()) {
                        String key = matcher.group(1);
                        Debug.println("key: " + key);
                        images.put(key, image);
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        });
        this.image = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        this.g = image.getGraphics();
    }

    Graphics g;

    BufferedImage image;

    public BufferedImage getImage() {
        createImage();
        return image;
    }

    private void createImage() {
        drawImage(images.get("体/00"));

        drawImage(images.get("眉/00")); // fixed
        drawImage(images.get("目/00")); // anime
        drawImage(images.get("顔/00a")); // fixed, blend
        drawImage(images.get("口/00")); // anime

        drawImage(images.get("他/02")); // fixed
    }

    private void drawImage(BufferedImage image) {
        int x = (W - image.getWidth()) / 2;
        int y = (H - image.getHeight()) / 2;
        g.drawImage(image, x, y, null);
    }
}
