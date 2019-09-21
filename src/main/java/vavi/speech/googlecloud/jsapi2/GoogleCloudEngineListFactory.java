/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlecloud.jsapi2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import com.google.cloud.texttospeech.v1.ListVoicesRequest;
import com.google.cloud.texttospeech.v1.ListVoicesResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;


/**
 * Factory for the Google Cloud engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public class GoogleCloudEngineListFactory implements EngineListFactory {

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
                new GoogleCloudTextToSpeechSynthesizerMode(null,
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
        for (com.google.cloud.texttospeech.v1.Voice nativeVoice : listAllSupportedVoices()) {
            Voice voice = new Voice(new SpeechLocale(nativeVoice.getLanguageCodes(0)),
                                    nativeVoice.getName(),
                                    nativeVoice.getSsmlGenderValue(),
                                    Voice.AGE_DONT_CARE,
                                    Voice.VARIANT_DONT_CARE);
            voiceList.add(voice);
        }
        return voiceList;
    }

    /** */
    static List<com.google.cloud.texttospeech.v1.Voice> listAllSupportedVoices() {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            ListVoicesRequest request = ListVoicesRequest.getDefaultInstance();

            ListVoicesResponse response = textToSpeechClient.listVoices(request);
            List<com.google.cloud.texttospeech.v1.Voice> voices = response.getVoicesList();

            return voices;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
