/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
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
import vavi.speech.modifier.yakuwarigo.YakuwarigoModifier;


/**
 * RococoaTest. (jsapi2, rococoa)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/21 umjammer initial version <br>
 */
class ZundamonTest {

    @Test
    void test1() throws Exception {
        main(new String[] {"src/test/resources/speech.txt"});
    }

    /**
     * @param args command line arguments.
     */
    public static void main(String[] args) throws Exception {
        Path file = Paths.get(args[0]);

        //
        YakuwarigoModifier.ConvertOption option = new YakuwarigoModifier.ConvertOption();
        option.disableKutenToExclamation = true;
        option.name = "zundamon";
        option.disablePrefix = true;
        option.disableLongNote = true;
        YakuwarigoModifier modifier = new YakuwarigoModifier(option);

        //
        EngineManager.registerEngineListFactory(vavi.speech.voicevox.jsapi2.VoiceVoxEngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).forEach(System.err::println);
        String voiceName = "ずんだもん(ノーマル)";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
        synthesizer.getSynthesizerProperties().setVolume(3);

        Files.lines(file).forEach(line -> {
            try {
                String text = modifier.convert(line);
                System.out.println(text);
                synthesizer.speak(text, System.err::println);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
