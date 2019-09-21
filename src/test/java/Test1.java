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

import vavi.speech.aquestalk10.jsapi2.AquesTalk10EngineListFactory;


/**
 * Test1. (jsapi2, aquestalk10)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/21 umjammer initial version <br>
 */
public final class Test1 {

    /**
     * @param args command line arguments.
     */
    public static void main(final String[] args) throws Exception {
//        String text = args[0];
        String text = "ゆっくりしていってね";

        EngineManager.registerEngineListFactory(AquesTalk10EngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(SynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        synthesizer.getSynthesizerProperties().setVolume(20);
        String voiceName = "F1";
        Voice voice = Arrays.asList(SynthesizerMode.class.cast(synthesizer.getEngineMode()).getVoices()).stream().filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(voice);

        System.out.println(text);
        synthesizer.speak(text, System.err::println);
        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();

        System.exit(0);
    }
}
