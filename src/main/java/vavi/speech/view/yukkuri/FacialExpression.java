/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.util.Locale;


/**
 * FacialExpression presets mapped to 神威式 character layer names.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public enum FacialExpression {

    NEUTRAL("デフォルト", "001デフォルト", "デフォルト", "00", null),
    HAPPY("デフォルト照", "014にっこり", "デフォルト", "01", null),
    EXCITED("デフォルト高揚", "046キラキラ", "デフォルト", "02", null),
    ANGRY("デフォルト", "002ジト目", "怒り", "03", "表情怒"),
    FURIOUS("デフォルト高揚", "003見開き", "激怒", "04", "表情怒"),
    SAD("デフォルト無", "023泣き", "困り", "05", "表情哀"),
    SURPRISED("デフォルト", "003見開き", "デフォルト", "06", null),
    SMUG("デフォルト", "002ジト目", "普", "07", null),
    PALE("デフォルト無", "016虚ろ目", "困り", "05", "表情蒼白"),
    DARK("デフォルト無", "016虚ろ目", "困り", "00", "表情暗黒"),
    WINK("デフォルト照", "008ウィンク", "デフォルト", "01", null);

    private final String body;
    private final String eye;
    private final String eyebrow;
    private final String mouthBase;
    private final String overlay;

    FacialExpression(String body, String eye, String eyebrow, String mouthBase, String overlay) {
        this.body = body;
        this.eye = eye;
        this.eyebrow = eyebrow;
        this.mouthBase = mouthBase;
        this.overlay = overlay;
    }

    public String body() {
        return body;
    }

    public String eye() {
        return eye;
    }

    public String eyebrow() {
        return eyebrow;
    }

    public String mouthBase() {
        return mouthBase;
    }

    public String overlay() {
        return overlay;
    }

    /**
     * Parses an expression name leniently, defaulting to {@link #NEUTRAL} when unknown.
     */
    public static FacialExpression fromString(String name) {
        if (name == null || name.isBlank()) {
            return NEUTRAL;
        }
        String normalized = name.trim().toUpperCase(Locale.ROOT);
        for (FacialExpression expr : values()) {
            if (expr.name().equals(normalized)) {
                return expr;
            }
        }
        return switch (normalized) {
            case "JOY", "SMILE", "GLAD" -> HAPPY;
            case "ANGER", "MAD" -> ANGRY;
            case "SORROW", "CRY" -> SAD;
            case "SURPRISE", "SHOCK" -> SURPRISED;
            case "FEAR", "SHOCKED" -> PALE;
            default -> NEUTRAL;
        };
    }
}
