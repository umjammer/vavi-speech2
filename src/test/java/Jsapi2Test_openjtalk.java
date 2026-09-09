/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import vavi.speech.openjtalk.jsapi2.OpenJTalkSynthesizer;
import vavi.speech.openjtalk.jsapi2.OpenJTalkSynthesizerMode;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


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
@DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*")
@PropsEntity(url = "file:local.properties")
class Jsapi2Test_openjtalk {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "openjtalk .txt")
    String text = "ゆっくりしていってね";

    @Property(name = "openjtalk .voice")
    String voiceName = "mei_happy";

    @Property(name = "openjtalk .volume")
    int volume = 3;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test01() throws Exception {
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new OpenJTalkSynthesizerMode());
        assertInstanceOf(OpenJTalkSynthesizer.class, synthesizer);

        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

//Arrays.stream(SynthesizerMode.class.cast(synthesizer.getEngineMode()).getVoices()).forEach(System.err::println);
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
     * @param args 0: text
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_openjtalk app = new Jsapi2Test_openjtalk();
        String text = args[0];
        app.speak(text);
    }
}
