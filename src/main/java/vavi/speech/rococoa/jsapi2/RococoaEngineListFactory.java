/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.rococoa.jsapi2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSObject;
import vavi.util.Debug;
import vavix.rococoa.avfoundation.AVSpeechSynthesisVoice;


/**
 * Factory for the Mac Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public class RococoaEngineListFactory implements EngineListFactory {

    @Override
    public EngineList createEngineList(EngineMode require) {
        if (require instanceof SynthesizerMode mode) {
            List<Voice> allVoices = getVoices();
            List<Voice> voices = new ArrayList<>();
            if (mode.getVoices() == null) {
                voices.addAll(allVoices);
            } else {
                for (Voice availableVoice : allVoices) {
                    for (Voice requiredVoice : mode.getVoices()) {
                        if (availableVoice.match(requiredVoice)) {
                            voices.add(availableVoice);
                        }
                    }
                }
            }
//voices.forEach(System.err::println);
            SynthesizerMode[] features = new SynthesizerMode[] {
                new RococoaSynthesizerMode(null,
                                       mode.getEngineName(),
                                       mode.getRunning(),
                                       mode.getSupportsLetterToSound(),
                                       mode.getMarkupSupport(),
                                       voices.toArray(Voice[]::new))
            };
            return new EngineList(features);
        }

        return null;
    }

    /**
     * Retrieves all voices.
     *
     * @return all voices
     */
    private static List<Voice> getVoices() {
        List<Voice> voiceList = new LinkedList<>();
        for (NSObject object : AVSpeechSynthesisVoice.speechVoices()) {
            AVSpeechSynthesisVoice nativeVoice = Rococoa.cast(object, AVSpeechSynthesisVoice.class);
            Voice voice = new Voice(getSpeechLocale(nativeVoice),
                                    nativeVoice.name(),
                                    getGender(nativeVoice),
                                    getAge(nativeVoice),
                                    Voice.VARIANT_DONT_CARE);
            voiceList.add(voice);
        }
        return voiceList;
    }

    /** */
    private static SpeechLocale getSpeechLocale(AVSpeechSynthesisVoice nativeVoice) {
        try {
            return new SpeechLocale(nativeVoice.identifier());
        } catch (NullPointerException e) {
Debug.println(Level.FINE, "getSpeechLocale: " + nativeVoice.name());
            return SpeechLocale.getDefault();
        }
    }

    /** */
    private static int getAge(AVSpeechSynthesisVoice nativeVoice) {
        return Voice.AGE_DONT_CARE;
    }

    /** */
    private static int getGender(AVSpeechSynthesisVoice nativeVoice) {
        int gender;

        try {
            gender = nativeVoice.gender();
        } catch (IllegalArgumentException e) {
Debug.println(Level.FINE, "getGender: " + nativeVoice.name());
            return Voice.GENDER_DONT_CARE;
        }

        return switch (gender) {
            case AVSpeechSynthesisVoice.AVSpeechSynthesisVoiceGenderFemale -> Voice.GENDER_FEMALE;
            case AVSpeechSynthesisVoice.AVSpeechSynthesisVoiceGenderMale -> Voice.GENDER_MALE;
            case AVSpeechSynthesisVoice.AVSpeechSynthesisVoiceGenderUnspecified -> Voice.GENDER_NEUTRAL;
            default -> Voice.GENDER_DONT_CARE;
        };
    }
}
