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

import vavi.speech.openjtalk.jsapi2.OpenJTalkEngineListFactory;


/**
 * Test5. (jsapi2, openjtalk)
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
public final class Test5 {

    /**
     * @param args command line arguments.
     */
    public static void main(final String[] args) throws Exception {
        String text = args[0];
//        String text = "ゆっくりしていってね";

        EngineManager.registerEngineListFactory(OpenJTalkEngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(SynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        synthesizer.getSynthesizerProperties().setVolume(20);
//Arrays.stream(SynthesizerMode.class.cast(synthesizer.getEngineMode()).getVoices()).forEach(System.err::println);
        String voiceName = "mei_happy";
        Voice voice = Arrays.stream(SynthesizerMode.class.cast(synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(voice);

        for (String line : text.split("。")) {
            System.out.println(line);
            synthesizer.speak(line, System.err::println);
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();

        System.exit(0);
    }
}
