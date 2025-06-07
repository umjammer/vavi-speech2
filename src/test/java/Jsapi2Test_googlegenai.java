/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.googlegenai.jsapi2.GoogleGenAITTSSynthesizer;
import vavi.speech.googlegenai.jsapi2.GoogleGenAITTSSynthesizerMode;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * Jsapi2Test_googlegenai. (jsapi2, google gen ai)
 * <p>
 * env
 * <li>GOOGLE_API_KEY ... api key
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 25/06/08 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
@EnabledIfEnvironmentVariable(named = "GOOGLE_API_KEY", matches = ".*")
class Jsapi2Test_googlegenai {

    static {
        System.setProperty("javax.speech.SpeechLocale.comparisonStrictness", "LENIENT");
//        System.setProperty("vavi.speech.googlegenai.model", "gemini-2.5-flash-preview-tts");
    }

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "googlegenai.txt")
    String text = "Say cheerfully: ゆっくりしていってね";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test01() throws Exception {
//text = new String(Jsapi2Test_googlegenai.class.getResourceAsStream("speech.txt").readAllBytes());
        speak();
    }

    /**
     * google gemini api doesn't mind locale, it's auto detected (currently support 30 languages)
     * @see "https://ai.google.dev/gemini-api/docs/speech-generation#voices"
     */
    void speak() throws Exception {
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new GoogleGenAITTSSynthesizerMode());
        assertInstanceOf(GoogleGenAITTSSynthesizer.class, synthesizer);

        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

//GoogleGenAITTSVoice.factory.getAllNativeVoices().forEach(System.out::println);
        String voiceName = "Kore";
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

    /**
     * @param args 0: text
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_googlegenai app = new Jsapi2Test_googlegenai();
        if (localPropertiesExists())
            PropsEntity.Util.bind(app);
        else
            app.text = args[0];
        app.speak();
    }
}
