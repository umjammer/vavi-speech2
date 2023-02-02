/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
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
import vavi.speech.voicevox.VoiceVox;
import vavi.util.Debug;


/**
 * Jsapi2Test_voicevox. (jsapi2, voicevox)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/01/14 umjammer initial version <br>
 */
@EnabledIf("localServerExists")
class Jsapi2Test_voicevox {

    static boolean localServerExists() {
        try {
            new VoiceVox();
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
        Jsapi2Test_voicevox app = new Jsapi2Test_voicevox();
        String text = args[0];
        app.speak(text);
    }

    @Test
    void test01() throws Exception {
        String text = "ゆっくりしやがれなのだ";
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        EngineManager.registerEngineListFactory(vavi.speech.voicevox.jsapi2.VoiceVoxEngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        String voiceName = "ずんだもん(ノーマル)";
//        String voiceName = "四国めたん(ツンツン)";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
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
