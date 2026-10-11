/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import vavi.speech.coeiroink.CoeiroInk;
import vavi.speech.coeiroink.jsapi2.CoeiroInkSynthesizer;
import vavi.speech.coeiroink.jsapi2.CoeiroInkSynthesizerMode;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * Jsapi2Test_coeiroink. (jsapi2, coeiroink)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2024/03/03 umjammer initial version <br>
 */
@EnabledIf("localServerExists") // make sure CoeiroInk.app is running
@PropsEntity(url = "file:local.properties")
class Jsapi2Test_coeiroink {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    static boolean localServerExists() {
        try {
            new CoeiroInk();
            return true;
        } catch (Exception e) {
Debug.println(Level.WARNING, e.getMessage());
            return false;
        }
    }

    @Property(name = "coeiroink.text")
    String text = "そこでつくよみちゃんの出番です！";

    @Property(name = "coeiroink.voice")
    String voiceName = "つくよみちゃん";

    @Property(name = "coeiroink.volume")
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
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new CoeiroInkSynthesizerMode());
        assertInstanceOf(CoeiroInkSynthesizer.class, synthesizer);

        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setSpeakingRate(100); // 50 ~ 100 ~ 200 TODO not spec
        synthesizer.getSynthesizerProperties().setPitch(16); // 1 ~ 16 ~ 31 TODO not spec
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
     * @param args command line arguments.
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_coeiroink app = new Jsapi2Test_coeiroink();
        String text = args[0];
        app.speak(text);
    }
}
