/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@code vavi.speech.view.yukkuri}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
class YukkuriTest_yukkuri {

    static boolean marisaExists() {
        return Files.isDirectory(Path.of("tmp/神威式魔理沙"));
    }

    static boolean reimuExists() {
        return Files.isDirectory(Path.of("tmp/神威式霊夢"));
    }

    static boolean voicevoxWavExists() {
        return Files.isRegularFile(Path.of("tmp/voicevox.wav"));
    }

    @Test
    @EnabledIf("marisaExists")
    void testLoadAndRenderMarisa() throws Exception {
        YukkuriCharacter marisa = new YukkuriCharacter(Path.of("tmp/神威式魔理沙"));
        assertFalse(marisa.getImages().isEmpty());

        for (FacialExpression expr : FacialExpression.values()) {
            marisa.setExpression(expr);
            for (LipSyncAnalyzer.Vowel vowel : LipSyncAnalyzer.Vowel.values()) {
                marisa.setVowel(vowel);
                BufferedImage img = marisa.getImage();
                assertNotNull(img);
                assertEquals(YukkuriCharacter.W, img.getWidth());
                assertEquals(YukkuriCharacter.H, img.getHeight());
                assertTrue(hasNonTransparentPixel(img), "Expected visible pixels for " + expr + "/" + vowel);
            }
        }
    }

    @Test
    @EnabledIf("reimuExists")
    void testLoadAndRenderReimu() throws Exception {
        YukkuriCharacter reimu = new YukkuriCharacter(Path.of("tmp/神威式霊夢"));
        assertFalse(reimu.getImages().isEmpty());

        reimu.setExpression(FacialExpression.HAPPY);
        reimu.setVowel(LipSyncAnalyzer.Vowel.A);
        reimu.setBlink(YukkuriCharacter.BlinkPhase.HALF);
        BufferedImage img = reimu.getImage();
        assertNotNull(img);
        assertTrue(hasNonTransparentPixel(img));
    }

    @Test
    void testLipSyncSilenceAndTone() {
        LipSyncAnalyzer analyzer = new LipSyncAnalyzer();
        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);

        // 1. Silence
        byte[] silence = new byte[16000 * 2];
        LipSyncTimeline silentTimeline = analyzer.analyze(silence, format);
        assertFalse(silentTimeline.frames().isEmpty());
        assertEquals(LipSyncAnalyzer.Vowel.SILENT, silentTimeline.at(100).vowel());

        // 2. Dual-formant synthetic vowel /a/ (750 Hz + 1250 Hz)
        int samples = 16000;
        byte[] pcm = new byte[samples * 2];
        for (int i = 0; i < samples; i++) {
            double t = i / 16000.0;
            double v = 0.4 * Math.sin(2 * Math.PI * 750 * t) + 0.3 * Math.sin(2 * Math.PI * 1250 * t);
            short s = (short) (v * 32767);
            pcm[i * 2] = (byte) (s & 0xFF);
            pcm[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        LipSyncTimeline vowelTimeline = analyzer.analyze(pcm, format);
        LipSyncAnalyzer.LipSyncFrame frame = vowelTimeline.at(200);
        assertEquals(LipSyncAnalyzer.Vowel.A, frame.vowel());
        assertTrue(frame.volume() > 0.1f);
    }

    @Test
    @EnabledIf("voicevoxWavExists")
    void testLipSyncWithVoicevoxWav() throws Exception {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(Path.of("tmp/voicevox.wav").toFile())) {
            AudioFormat format = ais.getFormat();
            byte[] pcm = ais.readAllBytes();
            LipSyncAnalyzer analyzer = new LipSyncAnalyzer();
            LipSyncTimeline timeline = analyzer.analyze(pcm, format);
            assertFalse(timeline.frames().isEmpty());
            boolean hasSpeech = timeline.frames().stream()
                    .anyMatch(f -> f.vowel() != LipSyncAnalyzer.Vowel.SILENT);
            assertTrue(hasSpeech, "Expected non-silent lip-sync frames in voicevox.wav");
        }
    }

    @Test
    void testAcpResponseParsing() throws Exception {
        String acpOutput = """
                Here is the timeline:
                ```json
                [
                  {"startRatio": 0.0, "endRatio": 0.5, "expression": "HAPPY", "segment": "こんにちは！"},
                  {"startRatio": 0.5, "endRatio": 1.0, "expression": "ANGRY", "segment": "ふざけるな！"}
                ]
                ```
                """;
        ExpressionTimeline timeline = AcpExpressionAnalyzer.parseResponse(acpOutput, 4000L);
        assertEquals(2, timeline.entries().size());
        assertEquals(FacialExpression.HAPPY, timeline.at(500));
        assertEquals(FacialExpression.ANGRY, timeline.at(2500));
    }

    @Test
    void testHeuristicExpressionAnalyzer() {
        HeuristicExpressionAnalyzer analyzer = new HeuristicExpressionAnalyzer();
        ExpressionTimeline timeline = analyzer.analyze("今日はとても嬉しいな！でもちょっと悲しい。", 3000L);
        assertEquals(2, timeline.entries().size());
        assertEquals(FacialExpression.HAPPY, timeline.at(100));
        assertEquals(FacialExpression.SAD, timeline.at(2500));
    }

    @Test
    @EnabledIf("marisaExists")
    void testPlayerSynchronizesCharacterState() throws Exception {
        YukkuriCharacter marisa = new YukkuriCharacter(Path.of("tmp/神威式魔理沙"));
        boolean[] updated = {false};
        YukkuriSpeechPlayer player = new YukkuriSpeechPlayer(
                marisa,
                () -> updated[0] = true,
                new HeuristicExpressionAnalyzer(),
                new LipSyncAnalyzer());

        LipSyncTimeline lipSync = new LipSyncTimeline(
                java.util.List.of(new LipSyncAnalyzer.LipSyncFrame(0, LipSyncAnalyzer.Vowel.I, 0.5f, 300f, 2200f)),
                1000,
                1000);
        ExpressionTimeline exprs = new ExpressionTimeline(
                java.util.List.of(new ExpressionTimeline.Entry(0, 1000, FacialExpression.SURPRISED, "えっ")));

        player.applyStateAt(100, lipSync, exprs);
        assertTrue(updated[0]);
        assertEquals(FacialExpression.SURPRISED, marisa.getExpression());
        assertEquals(LipSyncAnalyzer.Vowel.I, marisa.getVowel());
    }

    @Test
    @EnabledIf("marisaExists")
    void testDefaultExpressionIsSmileAndCanBeSet() throws Exception {
        YukkuriCharacter marisa = new YukkuriCharacter(Path.of("tmp/神威式魔理沙"));
        assertEquals(FacialExpression.HAPPY, marisa.getDefaultExpression());
        assertEquals(FacialExpression.HAPPY, marisa.getExpression());

        marisa.setDefaultExpression(FacialExpression.SMUG);
        assertEquals(FacialExpression.SMUG, marisa.getDefaultExpression());
        assertEquals(FacialExpression.SMUG, marisa.getExpression());
    }

    @Test
    void testAcpDefaultExpressionParsingAndAnalysis() throws Exception {
        assertEquals(FacialExpression.SAD, AcpExpressionAnalyzer.parseDefaultExpressionResponse("SAD"));
        assertEquals(FacialExpression.ANGRY, AcpExpressionAnalyzer.parseDefaultExpressionResponse("```\nANGRY\n```"));
        assertEquals(FacialExpression.HAPPY, AcpExpressionAnalyzer.parseDefaultExpressionResponse("smile"));

        AcpExpressionAnalyzer stub = new AcpExpressionAnalyzer() {
            @Override
            protected String queryAcp(String prompt) {
                return "EXCITED";
            }
        };
        assertEquals(FacialExpression.EXCITED, stub.analyzeDefaultExpression("やったー！最高！"));
        assertEquals(FacialExpression.HAPPY, stub.analyzeDefaultExpression(""));
    }

    private static boolean hasNonTransparentPixel(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y += 4) {
            for (int x = 0; x < img.getWidth(); x += 4) {
                if ((img.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
