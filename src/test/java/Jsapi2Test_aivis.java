/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.Arrays;
import java.util.logging.Level;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import vavi.speech.aivis.jsapi2.AivisSynthesizer;
import vavi.speech.aivis.jsapi2.AivisSynthesizerMode;
import vavi.speech.voicevox.VoiceVox;
import vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizer;
import vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * Jsapi2Test_aivis. (jsapi2, aivis)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2024/12/14 umjammer initial version <br>
 */
@EnabledIf("localServerExists")
class Jsapi2Test_aivis {

    static boolean localServerExists() {
        try (var dummy = new VoiceVox("http://127.0.0.1:10101/")) {
            return true;
        } catch (Exception e) {
Debug.println(Level.WARNING, e.getMessage());
            return false;
        }
    }

    /**
     * @param args command line arguments.
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_aivis app = new Jsapi2Test_aivis();
        String text = args[0];
        app.speak(text);
    }

    @Test
    void test01() throws Exception {
        String text = "成功の鍵は、タイミングと少しの運。不運だけは確実に訪れる。";
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new AivisSynthesizerMode());
        assertInstanceOf(AivisSynthesizer.class, synthesizer);

        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        String voiceName = "Anneli(ノーマル)";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setSpeakingRate(100); // 50 ~ 100 ~ 200
        synthesizer.getSynthesizerProperties().setPitch(16); // 1 ~ 16 ~ 31
        synthesizer.getSynthesizerProperties().setVoice(voice);
        synthesizer.getSynthesizerProperties().setVolume(3);

        for (String line : text.split("。")) {
            System.out.println(line);
            synthesizer.speak(line, System.err::println);
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
