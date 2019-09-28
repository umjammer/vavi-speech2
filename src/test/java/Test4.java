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


/**
 * Test4. (jsapi2, rococoa)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/21 umjammer initial version <br>
 */
public final class Test4 {

    /**
     * @param args command line arguments.
     */
    public static void main(final String[] args) throws Exception {
        Path file = Paths.get(args[0]);

//      EngineManager.registerEngineListFactory(vavi.speech.rococoa.jsapi2.RococoaEngineListFactory.class.getName());
        EngineManager.registerEngineListFactory(vavi.speech.googlecloud.jsapi2.GoogleCloudEngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(SynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        synthesizer.getSynthesizerProperties().setVolume(20);
//Arrays.asList(SynthesizerMode.class.cast(synthesizer.getEngineMode()).getVoices()).stream().forEach(System.err::println);
//        String voiceName = "Alex";
        String voiceName = "en-US-Wavenet-A";
        Voice voice = Arrays.asList(SynthesizerMode.class.cast(synthesizer.getEngineMode()).getVoices()).stream().filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));

        Files.lines(file).forEach(line -> {
            System.out.println(line);
            synthesizer.speak(line, System.err::println);
        });

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();

        System.exit(0);
    }
}
