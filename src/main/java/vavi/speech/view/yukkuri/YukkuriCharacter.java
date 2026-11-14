/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.imageio.ImageIO;


/**
 * Layered Yukkuri character renderer for {@code tmp/神威式魔理沙} and {@code tmp/神威式霊夢} assets.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class YukkuriCharacter {

    public static final int W = 500;
    public static final int H = 500;

    public enum Part {
        体, 目, 眉, 口, 他
    }

    public enum BlinkPhase {
        OPEN, HALF, CLOSED
    }

    private final String name;
    private final Map<String, BufferedImage> images = new HashMap<>();
    private BufferedImage frontBuffer;
    private BufferedImage backBuffer;
    private boolean dirty = true;

    private FacialExpression defaultExpression = FacialExpression.HAPPY;
    private FacialExpression expression = defaultExpression;
    private LipSyncAnalyzer.Vowel vowel = LipSyncAnalyzer.Vowel.SILENT;
    private BlinkPhase blink = BlinkPhase.OPEN;
    private String hair = "髪デフォルト";

    public YukkuriCharacter(Path characterDir) throws IOException {
        Objects.requireNonNull(characterDir, "characterDir");
        if (!Files.isDirectory(characterDir)) {
            throw new IOException("Character directory not found: " + characterDir);
        }
        this.name = normalize(characterDir.getFileName().toString());
        loadParts(characterDir);
        this.frontBuffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        this.backBuffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
    }

    private void loadParts(Path characterDir) throws IOException {
        for (Part part : Part.values()) {
            Path partDir = characterDir.resolve(part.name());
            if (!Files.isDirectory(partDir)) {
                continue;
            }
            try (Stream<Path> stream = Files.list(partDir)) {
                stream.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase().endsWith(".png"))
                        .forEach(p -> {
                            try {
                                String fileName = normalize(p.getFileName().toString());
                                String baseName = fileName.substring(0, fileName.length() - 4);
                                String key = part.name() + "/" + baseName;
                                BufferedImage img = ImageIO.read(p.toFile());
                                if (img != null) {
                                    images.put(key, img);
                                }
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, BufferedImage> getImages() {
        return Collections.unmodifiableMap(images);
    }

    public synchronized void setDefaultExpression(FacialExpression defaultExpression) {
        Objects.requireNonNull(defaultExpression, "defaultExpression");
        this.defaultExpression = defaultExpression;
        if (this.expression != defaultExpression) {
            this.expression = defaultExpression;
            this.dirty = true;
        }
    }

    public synchronized FacialExpression getDefaultExpression() {
        return defaultExpression;
    }

    public synchronized void setExpression(FacialExpression expression) {
        Objects.requireNonNull(expression, "expression");
        if (this.expression != expression) {
            this.expression = expression;
            this.dirty = true;
        }
    }

    public synchronized FacialExpression getExpression() {
        return expression;
    }

    public synchronized void setVowel(LipSyncAnalyzer.Vowel vowel) {
        Objects.requireNonNull(vowel, "vowel");
        if (this.vowel != vowel) {
            this.vowel = vowel;
            this.dirty = true;
        }
    }

    public synchronized LipSyncAnalyzer.Vowel getVowel() {
        return vowel;
    }

    public synchronized void setBlink(BlinkPhase blink) {
        Objects.requireNonNull(blink, "blink");
        if (this.blink != blink) {
            this.blink = blink;
            this.dirty = true;
        }
    }

    public synchronized BlinkPhase getBlink() {
        return blink;
    }

    public synchronized void setHair(String hair) {
        Objects.requireNonNull(hair, "hair");
        if (!this.hair.equals(hair)) {
            this.hair = hair;
            this.dirty = true;
        }
    }

    /**
     * Atomically updates expression, vowel, and blink state.
     *
     * @return {@code true} if any state field changed
     */
    public synchronized boolean setState(FacialExpression expression, LipSyncAnalyzer.Vowel vowel, BlinkPhase blink) {
        Objects.requireNonNull(expression, "expression");
        Objects.requireNonNull(vowel, "vowel");
        Objects.requireNonNull(blink, "blink");
        if (this.expression == expression && this.vowel == vowel && this.blink == blink) {
            return false;
        }
        this.expression = expression;
        this.vowel = vowel;
        this.blink = blink;
        this.dirty = true;
        return true;
    }

    /**
     * Renders the current facial expression, eye blink, and lip-sync mouth onto the 500x500 RGBA canvas.
     */
    public synchronized BufferedImage getImage() {
        if (!dirty) {
            return frontBuffer;
        }
        Graphics2D g = backBuffer.createGraphics();
        try {
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, W, H);
            g.setComposite(AlphaComposite.SrcOver);

            drawPart(g, Part.体, expression.body(), "デフォルト");
            drawPart(g, Part.目, resolveEyeKey(), "001デフォルト");
            drawPart(g, Part.口, resolveMouthKey(), "00");
            drawPart(g, Part.眉, expression.eyebrow(), "デフォルト");
            if (expression.overlay() != null) {
                drawPart(g, Part.他, expression.overlay(), null);
            }
            drawPart(g, Part.他, hair, "髪デフォルト");
        } finally {
            g.dispose();
        }
        BufferedImage tmp = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = tmp;
        dirty = false;
        return frontBuffer;
    }

    private String resolveEyeKey() {
        String base = expression.eye();
        return switch (blink) {
            case OPEN -> base;
            case HALF -> hasPart(Part.目, base + ".0") ? base + ".0" : base;
            case CLOSED -> {
                if (hasPart(Part.目, base + ".1")) {
                    yield base + ".1";
                }
                yield hasPart(Part.目, "015閉じ") ? "015閉じ" : base;
            }
        };
    }

    private String resolveMouthKey() {
        String base = expression.mouthBase();
        String suffix = switch (vowel) {
            case SILENT -> "";
            case E -> ".0";
            case I -> ".1";
            case A -> ".2";
            case U -> ".3";
            case O -> ".4";
        };
        if (suffix.isEmpty()) {
            return base;
        }
        if (hasPart(Part.口, base + suffix)) {
            return base + suffix;
        }
        if (hasPart(Part.口, "00" + suffix)) {
            return "00" + suffix;
        }
        if (vowel == LipSyncAnalyzer.Vowel.O && hasPart(Part.口, base + ".3")) {
            return base + ".3";
        }
        return base;
    }

    private boolean hasPart(Part part, String key) {
        return images.containsKey(part.name() + "/" + normalize(key));
    }

    private void drawPart(Graphics2D g, Part part, String key, String fallbackKey) {
        BufferedImage img = key != null ? images.get(part.name() + "/" + normalize(key)) : null;
        if (img == null && fallbackKey != null) {
            img = images.get(part.name() + "/" + normalize(fallbackKey));
        }
        if (img != null) {
            int x = (W - img.getWidth()) / 2;
            int y = (H - img.getHeight()) / 2;
            g.drawImage(img, x, y, null);
        }
    }

    private static String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }
}
