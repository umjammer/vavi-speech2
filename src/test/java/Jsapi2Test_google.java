/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.Arrays;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.junit.jupiter.api.Test;
import vavi.speech.googlecloud.jsapi2.GoogleCloudTextToSpeechSynthesizer;
import vavi.speech.googlecloud.jsapi2.GoogleCloudTextToSpeechSynthesizerMode;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * Jsapi2Test_google. (jsapi2, google cloud)
 * <p>
 * env
 * <li>GOOGLE_APPLICATION_CREDENTIALS ... credential (json) path
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/21 umjammer initial version <br>
 */
class Jsapi2Test_google {

    static {
        System.setProperty("javax.speech.SpeechLocale.comparisonStrictness", "LENIENT");
    }

    /**
     * @param args 0: text
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_google app = new Jsapi2Test_google();
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
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new GoogleCloudTextToSpeechSynthesizerMode(new SpeechLocale("ja")));
        assertInstanceOf(GoogleCloudTextToSpeechSynthesizer.class, synthesizer);

        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

//GoogleCloudTextToSpeechVoice.factory.getAllNativeVoices().stream().filter(v -> v.getLanguageCodes(0).contains("ja")).forEach(System.out::println);
//        String voiceName = "en-US-Wavenet-A";
        String voiceName = "ja-JP-Wavenet-B";
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
