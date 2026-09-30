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

import vavi.speech.qwen3tts.Qwen3Tts;
import vavi.speech.qwen3tts.jsapi2.Qwen3TtsSynthesizer;
import vavi.speech.qwen3tts.jsapi2.Qwen3TtsSynthesizerMode;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * Jsapi2Test_qwen3tts. (jsapi2, qwen3tts)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2026/02/13 umjammer initial version <br>
 */
@EnabledIf("localServerExists") // make sure VoiceVox.app is running
@PropsEntity(url = "file:local.properties")
class Jsapi2Test_qwen3tts {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "qwen3tts.refAudio")
    String qwen3ttsRefAudio;

    @Property(name = "qwen3tts.refText")
    String qwen3ttsRefText;

    @Property(name = "qwen3tts.clone")
    String qwen3ttsClone;

    @Property(name = "qwen3tts.language")
    String qwen3ttsLanguage;

    static boolean localServerExists() {
        try {
            new Qwen3Tts();
            return true;
        } catch (Exception e) {
Debug.println(Level.WARNING, e.getMessage());
            return false;
        }
    }

    @Property(name = "qwen3tts.txt")
    String text = "この湖こんなに広かったかしら？　霧で見通しが悪くて困ったわ。もしかして私って方向音痴？";

    @Property(name = "qwen3tts.voice")
    String voiceName = "Vivian";

    @Property(name = "qwen3tts.volume")
    int volume = 100;

    @BeforeEach
    void setUp() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        System.setProperty("vavi.speech.qwen3tts.refAudio", qwen3ttsRefAudio);
        System.setProperty("vavi.speech.qwen3tts.refText", qwen3ttsRefText);
        System.setProperty("vavi.speech.qwen3tts.clone", qwen3ttsClone);
        System.setProperty("vavi.speech.qwen3tts.language", qwen3ttsLanguage);
Debug.print("vavi.speech.qwen3tts.ref: " + qwen3ttsRefAudio + ", vavi.speech.qwen3tts.clone: " + qwen3ttsClone);
    }

    @Test
    void test01() throws Exception {
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new Qwen3TtsSynthesizerMode());
        assertInstanceOf(Qwen3TtsSynthesizer.class, synthesizer);

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
        Jsapi2Test_qwen3tts app = new Jsapi2Test_qwen3tts();
        String text = args[0];
        app.speak(text);
    }
}
