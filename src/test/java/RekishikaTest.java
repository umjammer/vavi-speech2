/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
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


/**
 * RekishikaTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-02-01 nsano initial version <br>
 */
public class RekishikaTest {

    @Test
    void test1() throws Exception {
        main(new String[] {"宇宙はバチクソ面白いので"});
    }

    /**
     * @param args 0: text
     * @see "vavi.speech.voicevox.VoiceVoxTest#test5"
     */
    public static void main(String[] args) throws Exception {
        String text = args[0];

        //
        EngineManager.registerEngineListFactory(vavi.speech.voicevox.jsapi2.VoiceVoxEngineListFactory.class.getName());

        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(SynthesizerMode.DEFAULT);
        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).forEach(System.err::println);
        String voiceName = "櫻歌ミコ(ノーマル)";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
        synthesizer.getSynthesizerProperties().setVolume(3);
        synthesizer.getSynthesizerProperties().setSpeakingRate(192); // [wpm] eng: {200}
        synthesizer.getSynthesizerProperties().setPitch(69); // [Hz] male: 80 ~ 180, female: {150} ~ 300
        synthesizer.getSynthesizerProperties().setPitchRange(1); // 20% ~ 80% of pitch

        //
        // TODO adapt parameters
        //
        // 話速:  0.50 ~ 0.96 ~ 2.00
        // 音高: -0.15 ~ 0.08 ~ 0.15
        // 抑揚:  0    ~ 0.69 ~ 2
        //

        Arrays.stream(text.split("\n")).forEach(line -> {
            System.out.println(line);
            synthesizer.speak(line, System.err::println);
        });

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
