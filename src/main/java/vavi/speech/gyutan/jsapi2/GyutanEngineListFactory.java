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


/**
 * Factory for the Gyutan (OpenJTalk in Java) Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/01/20 umjammer initial version <br>
 */
public class GyutanEngineListFactory implements EngineListFactory {

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
            SynthesizerMode[] features = new SynthesizerMode[] {
                new GyutanSynthesizerMode(null,
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
        Scanner s = new Scanner(GyutanEngineListFactory.class.getResourceAsStream("/htsvoice.csv"));
        while (s.hasNextLine()) {
            String[] parts = s.nextLine().split(",");

            Voice voice = new Voice(new SpeechLocale(Locale.JAPAN.toString()),
                    parts[1],
                    toGender(parts[2]),
                    toAge(parts[3]),
                    Voice.VARIANT_DONT_CARE);
            voiceList.add(voice);
        }
        return voiceList;
    }

    /** */
    private static int toGender(String gender) {
        return switch (gender.toLowerCase()) {
            case "male" -> Voice.GENDER_MALE;
            case "female" -> Voice.GENDER_FEMALE;
            case "neutral" -> Voice.GENDER_NEUTRAL;
            default -> Voice.GENDER_DONT_CARE;
        };
    }

    /** */
    private static int toAge(String age) {
        return switch (age.toLowerCase()) {
            case "chile" -> Voice.AGE_CHILD;
            case "teenager" -> Voice.AGE_TEENAGER;
            case "younger_adult" -> Voice.AGE_YOUNGER_ADULT;
            case "middle_adult" -> Voice.AGE_MIDDLE_ADULT;
            case "older_adult" -> Voice.AGE_OLDER_ADULT;
            case "unknown" -> Voice.AGE_DONT_CARE;
            default -> Integer.parseInt(age);
        };
    }
}
