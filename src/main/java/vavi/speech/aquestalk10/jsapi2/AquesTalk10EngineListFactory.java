/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.aquestalk10.jsapi2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.aquestalk10.jna.AquesTalk10.AQTK_VOICE;
import vavi.speech.aquestalk10.jna.AquesTalk10Wrapper;


/**
 * Factory for the AquesTalk10 Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public class AquesTalk10EngineListFactory implements EngineListFactory {

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
            final SynthesizerMode[] features = new SynthesizerMode[] {
                new AquesTalk10SynthesizerMode(null,
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
        for (Map.Entry<String, AQTK_VOICE> aquesTalkVoice : AquesTalk10Wrapper.voices.entrySet()) {
            Voice voice = new Voice(new SpeechLocale(Locale.JAPAN.toString()),
                                    aquesTalkVoice.getKey(),
                                    toGender(aquesTalkVoice.getValue()),
                                    Voice.AGE_DONT_CARE,
                                    Voice.VARIANT_DONT_CARE);
            voiceList.add(voice);
        }
        return voiceList;
    }

    /** */
    private static int toGender(AQTK_VOICE voice) {
        switch (voice.bas) {
        case 0: return Voice.GENDER_FEMALE;
        case 1: return Voice.GENDER_FEMALE;
        case 2: return Voice.GENDER_MALE;
        default: return Voice.GENDER_DONT_CARE;
        }
    }
}
