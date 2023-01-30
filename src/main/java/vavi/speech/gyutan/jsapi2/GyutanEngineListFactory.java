/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.gyutan.jsapi2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.openjtalk.OpenJTalkWrapper;


/**
 * Factory for the Gyutan (OpenJTalk in Java) Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/01/20 umjammer initial version <br>
 */
public class GyutanEngineListFactory implements EngineListFactory {

    @Override
    public EngineList createEngineList(EngineMode require) {
        if (require instanceof SynthesizerMode) {
            SynthesizerMode mode = (SynthesizerMode) require;
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
            SynthesizerMode[] features = new SynthesizerMode[] {
                new GyutanSynthesizerMode(null,
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
        Scanner s = new Scanner(GyutanEngineListFactory.class.getResourceAsStream("/htsvoice.csv"));
        while (s.hasNextLine()) {
            String[] parts = s.nextLine().split(",");

            Voice voice = new Voice(new SpeechLocale(Locale.JAPAN.toString()),
                    parts[1],
                    toGendaer(parts[2]),
                    toAge(parts[3]),
                    Voice.VARIANT_DONT_CARE);
            voiceList.add(voice);
        }
        return voiceList;
    }

    /** */
    private static int toGendaer(String gender) {
        switch (gender.toLowerCase()) {
        case "male": return Voice.GENDER_MALE;
        case "female": return Voice.GENDER_FEMALE;
        case "neutral": return Voice.GENDER_NEUTRAL;
        default: return Voice.GENDER_DONT_CARE;
        }
    }

    /** */
    private static int toAge(String age) {
        switch (age.toLowerCase()) {
        case "chile": return Voice.AGE_CHILD;
        case "teenager": return Voice.AGE_TEENAGER;
        case "younger_adult": return Voice.AGE_YOUNGER_ADULT;
        case "middle_adult": return Voice.AGE_MIDDLE_ADULT;
        case "older_adult": return Voice.AGE_OLDER_ADULT;
        case "unknown": return Voice.AGE_DONT_CARE;
        default: return Integer.parseInt(age);
        }
    }
}
