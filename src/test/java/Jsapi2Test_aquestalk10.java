/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Jsapi2Test_aquestalk10. (jsapi2, aquestalk10)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/21 umjammer initial version <br>
 */
@EnabledIfSystemProperty(named = "os.arch", matches = "x86_64")
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

    @Test
    void test02() throws Exception {
        Path path = Paths.get("tmp/repezen.txt");
        String text = String.join("\n", Files.readAllLines(path));
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        EngineManager.registerEngineListFactory(vavi.speech.aquestalk10.jsapi2.AquesTalk10EngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(SynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        String voiceName = "F1";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(voice);
        synthesizer.getSynthesizerProperties().setVolume(2);

        for (String line : text.split("[。\n]")) {
            System.out.println(line);
            synthesizer.speak(line + "。", System.err::println);
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
