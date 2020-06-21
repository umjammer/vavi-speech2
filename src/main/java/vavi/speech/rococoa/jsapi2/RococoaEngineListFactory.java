/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.rococoa.jsapi2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import org.rococoa.contrib.appkit.NSSpeechSynthesizer;
import org.rococoa.contrib.appkit.NSVoice;


/**
 * Factory for the Mac Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public class RococoaEngineListFactory implements EngineListFactory {

    /* */
    @Override
    public EngineList createEngineList(final EngineMode require) {
        if (require instanceof SynthesizerMode) {
            final SynthesizerMode mode = (SynthesizerMode) require;
            final List<Voice> allVoices = getVoices();
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
            final SynthesizerMode[] features = new SynthesizerMode[] {
                new RococoaSynthesizerMode(null,
                                       mode.getEngineName(),
                                       mode.getRunning(),
                                       mode.getSupportsLetterToSound(),
                                       mode.getMarkupSupport(),
                                       voices.toArray(new Voice[0]))
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
    private List<Voice> getVoices() {
        List<Voice> voiceList = new LinkedList<>();
        for (NSVoice nativeVoice : NSSpeechSynthesizer.availableVoices()) {
            Voice voice = new Voice(new SpeechLocale(nativeVoice.getLocaleIdentifier()),
                                    nativeVoice.getName(),
                                    toGenger(nativeVoice.getGender()),
                                    nativeVoice.getAge(),
                                    Voice.VARIANT_DONT_CARE);
            voiceList.add(voice);
        }
        return voiceList;
    }

    /** */
    private static int toGenger(NSVoice.VoiceGender gender) {
        switch (gender) {
        case Female: return Voice.GENDER_FEMALE;
        case Male: return Voice.GENDER_MALE;
        case Neuter: return Voice.GENDER_NEUTRAL;
        default: return Voice.GENDER_DONT_CARE;
        }
    }
}
