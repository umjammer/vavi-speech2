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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import vavi.speech.aquestalk10.jsapi2.AquesTalk10EngineListFactory;


/**
 * Jsapi2Test_aquestalk10. (jsapi2, aquestalk10)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/21 umjammer initial version <br>
 */
@DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*")
class Jsapi2Test_aquestalk10 {

    /**
     * @param args 0: text
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_aquestalk10 app = new Jsapi2Test_aquestalk10();
        String text = args[0];
        app.speak(text);
    }

    @Test
    void test01() throws Exception {
        String text = "ゆっくりしていってね。" +
        "コ’ンカイわこのゲームをジッキョーした’いとオモイま’す。";
//        + "良い例　：コ’ンカイ+わ/この+ゲーム+を/ジッキョー+した’い+と/オモイ+ま’す。";
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        EngineManager.registerEngineListFactory(AquesTalk10EngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(SynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        synthesizer.getSynthesizerProperties().setVolume(10);
        String voiceName = "F1";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(voice);

        for (String line : text.split("。")) {
            System.out.println(line);
            synthesizer.speak(line + "。", System.err::println);
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
