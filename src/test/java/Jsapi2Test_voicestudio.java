/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.voicestudio.VoiceStudio;
import vavi.speech.voicestudio.jsapi2.VoiceStudioSynthesizer;
import vavi.speech.voicestudio.jsapi2.VoiceStudioSynthesizerMode;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * Jsapi2Test_voicestudio. (jsapi2, VoiceStudio)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2026/10/11 umjammer initial version <br>
 */
@EnabledIf("localServerExists") // make sure VoiceStudio.app is running
@PropsEntity(url = "file:local.properties")
class Jsapi2Test_voicestudio {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "voicestudio.refAudio")
    String voiceStudioRefAudio;

    @Property(name = "voicestudio.refText")
    String voiceStudioRefText;

    @Property(name = "voicestudio.clone")
    String voiceStudioClone;

    @Property(name = "voicestudio.language")
    String voiceStudioLanguage;

    static boolean localServerExists() {
        try {
            new VoiceStudio();
            return true;
        } catch (Exception e) {
Debug.println(Level.WARNING, e.getMessage());
            return false;
        }
    }

    @Property(name = "voicestudio.txt")
    String text = "この湖こんなに広かったかしら？　霧で見通しが悪くて困ったわ。もしかして私って方向音痴？";

    @Property(name = "voicestudio.voice")
    String voiceName = "ゆっくり霊夢";

    @Property(name = "voicestudio.volume")
    int volume = 100;

    @BeforeEach
    void setUp() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        if (voiceStudioRefAudio != null) System.setProperty("vavi.speech.voicestudio.refAudio", voiceStudioRefAudio);
        if (voiceStudioRefText != null) System.setProperty("vavi.speech.voicestudio.refText", voiceStudioRefText);
        if (voiceStudioClone != null) System.setProperty("vavi.speech.voicestudio.clone", voiceStudioClone);
        if (voiceStudioLanguage != null) System.setProperty("vavi.speech.voicestudio.language", voiceStudioLanguage);
Debug.print("vavi.speech.voicestudio.ref: " + voiceStudioRefAudio + ", vavi.speech.voicestudio.clone: " + voiceStudioClone);
    }

    @Test
    void test01() throws Exception {
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new VoiceStudioSynthesizerMode());
        assertInstanceOf(VoiceStudioSynthesizer.class, synthesizer);

        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(voice);
        synthesizer.getSynthesizerProperties().setVolume(volume);

        for (String line : text.split("。")) {
            System.out.println(line);
            synthesizer.speak(line, System.err::println);
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }

    /**
     * @param args command line arguments.
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_voicestudio app = new Jsapi2Test_voicestudio();
        String text = args[0];
        app.speak(text);
    }
}
