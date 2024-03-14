/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import vavi.speech.rococoa.jsapi2.RococoaSynthesizer;
import vavi.speech.rococoa.jsapi2.RococoaSynthesizerMode;
import vavi.util.Debug;
import vavix.rococoa.avfoundation.AVSpeechSynthesisVoice;
import vavix.rococoa.avfoundation.AVSpeechSynthesizer;
import vavix.rococoa.avfoundation.AVSpeechUtterance;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


/**
 * Jsapi2Test_rococoa. (jsapi2, rococoa)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/21 umjammer initial version <br>
 */
@EnabledOnOs(OS.MAC)
@DisabledIfSystemProperty(named = "os.arch", matches = "x86_64") // currently rococoa doesn't work on x86_64
@DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*")
class Jsapi2Test_rococoa {

    static {
        System.setProperty("javax.speech.SpeechLocale.comparisonStrictness", "LENIENT");

        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod",
                "sun\\.util\\.logging\\.internal\\.LoggingProviderImpl.*#log");
    }

    /**
     * @param args 0: text
     */
    public static void main(String[] args) throws Exception {
        Jsapi2Test_rococoa app = new Jsapi2Test_rococoa();
        String text = args[0];
        app.speak(text);
    }

    @Test
    @DisplayName("spi")
    @DisabledIfSystemProperty(named = "os.arch", matches = "x86_64")
    void test01() throws Exception {
        String text = "ゆっくりしていってね";
        speak(text);
    }

    /** */
    void speak(String text) throws Exception {
        Synthesizer synthesizer = (Synthesizer) EngineManager.createEngine(new RococoaSynthesizerMode(new SpeechLocale("ja")));
        assertInstanceOf(RococoaSynthesizer.class, synthesizer);

        synthesizer.addSynthesizerListener(System.err::println);
        synthesizer.allocate();
        synthesizer.waitEngineState(Engine.ALLOCATED);
        synthesizer.resume();
        synthesizer.waitEngineState(Synthesizer.RESUMED);

        String voiceName = "O-Ren";
        Voice voice = Arrays.stream(((SynthesizerMode) synthesizer.getEngineMode()).getVoices()).filter(v -> v.getName().equals(voiceName)).findFirst().get();
Debug.println(voice.getName());
        // to specify exact age doesn't work.
        synthesizer.getSynthesizerProperties().setVoice(new Voice(voice.getSpeechLocale(), voice.getName(), voice.getGender(), Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE));
        synthesizer.getSynthesizerProperties().setVolume(20); // 0 ~ 100

        for (String line : text.split("。")) {
            System.out.println(line);
            synthesizer.speak(line, System.err::println);
        }

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }

    @Test
    @Disabled("temporary: create zero length wave file")
    void test02() throws Exception {
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(new byte[0]), new AudioFormat(44100, 16, 2, true, false), 0);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, Paths.get("tmp/zero.wav").toFile());
        ais.close();
    }

    @Test
    @DisplayName("direct")
    @DisabledIfSystemProperty(named = "os.arch", matches = "x86_64")
    void test1() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);

        AVSpeechSynthesizer synthesizer = AVSpeechSynthesizer.newInstance();
        synthesizer.setDelegate(new AVSpeechSynthesizer.AVSpeechSynthesizerAdapter() {
            @Override public void speechSynthesizer_didFinishSpeechUtterance(AVSpeechSynthesizer sender, AVSpeechUtterance utterance) {
                cdl.countDown();
            }
        });

        AVSpeechSynthesisVoice voice = AVSpeechSynthesisVoice.withLanguage("ja_JP");
Debug.println("voice: " + voice);

        AVSpeechUtterance utterance = AVSpeechUtterance.of("ゆっくりしていってね");
        utterance.setVoice(voice);
        utterance.setVolume(.2f);
        utterance.setRate(.5f);
        utterance.setPitchMultiplier(1.0f);

        synthesizer.speakUtterance(utterance);

        cdl.await();
    }
}
