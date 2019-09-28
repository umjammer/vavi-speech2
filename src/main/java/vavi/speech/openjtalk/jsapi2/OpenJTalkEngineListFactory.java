/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.openjtalk.jsapi2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.openjtalk.OpenJTalkWrapper;


/**
 * Factory for the OpenJTalk Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/26 umjammer initial version <br>
 */
public class OpenJTalkEngineListFactory implements EngineListFactory {

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
            final SynthesizerMode[] features = new SynthesizerMode[] {
                new OpenJTalkSynthesizerMode(null,
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
        for (OpenJTalkWrapper.VoiceFileInfo jTalkVoice : new OpenJTalkWrapper().getVoices()) {
            Voice voice = new Voice(new SpeechLocale(Locale.JAPAN.toString()),
                                    jTalkVoice.name,
                                    Voice.GENDER_DONT_CARE,
                                    Voice.AGE_DONT_CARE,
                                    Voice.VARIANT_DONT_CARE);
            voiceList.add(voice);
        }
        return voiceList;
    }
}
