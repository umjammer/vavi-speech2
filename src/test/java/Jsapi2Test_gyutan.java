/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.speech.gyutan.jsapi2.GyutanSynthesizer;
import vavi.speech.gyutan.jsapi2.GyutanSynthesizerMode;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * Jsapi2Test_gyutan. (jsapi2, gyutan)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/01/14 umjammer initial version <br>
 */
@DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*") // 'cause this needs "sen.home"
@PropsEntity(url = "file:local.properties")
class Jsapi2Test_gyutan {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "gyutan.txt")
    String text = "すもももももももものうち";

    @Property(name = "gyutan.voice")
    String voiceName = "tohoku(neutral)";

    @Property(name = "volume.voice")
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

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test02() throws Exception {
        String text = new String(Files.readAllBytes(Paths.get("src/test/resources/speech.txt")));
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new GyutanSynthesizerMode());
        assertInstanceOf(GyutanSynthesizer.class, synthesizer);

        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

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
        Jsapi2Test_gyutan app = new Jsapi2Test_gyutan();
        String text = args[0];
        app.speak(text);
    }
}
