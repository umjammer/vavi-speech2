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
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.junit.jupiter.api.Test;
import vavi.speech.rococoa.jsapi2.RococoaSynthesizer;
import vavi.speech.rococoa.jsapi2.RococoaSynthesizerMode;


/**
 * RococoaTest. (jsapi2, rococoa)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/21 umjammer initial version <br>
 */
class RococoaTest {

    static {
        System.setProperty("javax.speech.SpeechLocale.comparisonStrictness", "LENIENT");
    }

    @Test
    void test1() throws Exception {
        main(new String[] {"src/test/resources/speech.txt"});
    }

    /**
     * @param args 0: text
     */
    public static void main(String[] args) throws Exception {
        Path file = Paths.get(args[0]);

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new RococoaSynthesizerMode(new SpeechLocale("ja")));
        assert synthesizer instanceof RococoaSynthesizer : String.valueOf(synthesizer);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).forEach(System.err::println);
        String voiceName = "O-Ren";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
        synthesizer.getSynthesizerProperties().setVolume(20);

        Files.lines(file).forEach(line -> {
            System.out.println(line);
            synthesizer.speak(line, System.err::println);
        });

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
