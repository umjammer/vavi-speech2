/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.Arrays;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * Jsapi2Test_openjtalk. (jsapi2, openjtalk)
 * <ul>
 * <li>"mei_angry"
 * <li>"mei_normal"
 * <li>"mei_happy"
 * <li>"mei_sad"
 * <li>"mei_bashful"
 * <li>"tohoku-f01-angry"
 * <li>"tohoku-f01-neutral"
 * <li>"tohoku-f01-sad"
 * <li>"tohoku-f01-happy"
 * <li>"nitech_jp_atr503_m001"
 * </ul>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/26 umjammer initial version <br>
 */
@Disabled("because installing jtalk is dull")
//@DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*")
class Jsapi2Test_openjtalk {

    /**
     * @param args 0: text
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_openjtalk app = new Jsapi2Test_openjtalk();
        String text = args[0];
        app.speak(text);
    }

    @Test
    void test01() throws Exception {
        String text = "ゆっくりしていってね";
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        EngineManager.registerEngineListFactory(vavi.speech.openjtalk.jsapi2.OpenJTalkEngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(SynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

//Arrays.stream(SynthesizerMode.class.cast(synthesizer.getEngineMode()).getVoices()).forEach(System.err::println);
        String voiceName = "mei_happy";
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
