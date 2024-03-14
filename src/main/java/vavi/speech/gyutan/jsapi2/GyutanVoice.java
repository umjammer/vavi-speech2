/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.gyutan.jsapi2;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;

import static java.lang.System.getLogger;


/**
 * GyutanVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-14 nsano initial version <br>
 */
public class GyutanVoice extends WrappedVoice<String[]> {

    private static final Logger logger = getLogger(GyutanVoice.class.getName());

    /** */
    public static final GyutanVoice factory = new GyutanVoice();

    /** for factory use only */
    private GyutanVoice() {
        super(null);
    }

    /** */
    protected GyutanVoice(String[] nativeVoice) {
        super(new SpeechLocale(Locale.JAPAN.toString()),
                nativeVoice[1],
                toGender(nativeVoice[2]),
                toAge(nativeVoice[3]),
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    @Override
    public List<String[]> getAllNativeVoices() {
        return nativeVoices;
    }

    @Override
    public List<WrappedVoice<String[]>> getAllVoices() {
        List<WrappedVoice<String[]>> voices = new ArrayList<>();
        for (String[] nativeVoice : nativeVoices) {
            WrappedVoice<String[]> voice = new GyutanVoice(nativeVoice);
            voices.add(voice);
        }
        return voices;
    }

    /** */
    private static final List<String[]> nativeVoices = new ArrayList<>();

    static {
        try {
            Scanner s = new Scanner(GyutanEngineListFactory.class.getResourceAsStream("/htsvoice.csv"));
            while (s.hasNextLine()) {
                String[] parts = s.nextLine().split(",");

                nativeVoices.add(parts);
            }
        } catch (NullPointerException e) {
            logger.log(Level.WARNING, "no gyutan voices definition files 'htsvoice.csv' file in classpath");
        }
    }

    @Override
    public String getDomain() {
        return "general";
    }

    @Override
    public Locale getLocale() {
        return Locale.JAPANESE;
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
