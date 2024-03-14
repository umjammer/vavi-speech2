/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.rococoa.jsapi2;

import java.lang.System.Logger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSObject;
import vavi.speech.WrappedVoice;
import vavi.util.Debug;
import vavix.rococoa.avfoundation.AVSpeechSynthesisVoice;

import static java.lang.System.getLogger;
import static vavix.rococoa.avfoundation.AVSpeechSynthesisVoice.AVSpeechSynthesisVoiceGenderFemale;
import static vavix.rococoa.avfoundation.AVSpeechSynthesisVoice.AVSpeechSynthesisVoiceGenderMale;
import static vavix.rococoa.avfoundation.AVSpeechSynthesisVoice.AVSpeechSynthesisVoiceGenderUnspecified;


/**
 * RococoaVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-14 nsano initial version <br>
 */
public class RococoaVoice extends WrappedVoice<AVSpeechSynthesisVoice> {

    private static final Logger logger = getLogger(RococoaVoice.class.getName());

    /** */
    public static final RococoaVoice factory = new RococoaVoice();

    /** for factory use only */
    private RococoaVoice() {
        super(null);
    }

    /** */
    protected RococoaVoice(AVSpeechSynthesisVoice nativeVoice) {
        super(getSpeechLocale(nativeVoice),
                nativeVoice.name(),
                getGender(nativeVoice),
                getAge(nativeVoice),
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    @Override
    public List<AVSpeechSynthesisVoice> getAllNativeVoices() {
try {
            return AVSpeechSynthesisVoice.speechVoices();
} catch (Throwable e) {
 logger.log(Logger.Level.WARNING, "this exception may be caused running on x86_64 chip", e);
 return Collections.emptyList();
}
    }

    @Override
    public List<WrappedVoice<AVSpeechSynthesisVoice>> getAllVoices() {
        List<WrappedVoice<AVSpeechSynthesisVoice>> voiceList = new LinkedList<>();
try {
        for (NSObject object : AVSpeechSynthesisVoice.speechVoices()) {
            AVSpeechSynthesisVoice nativeVoice = Rococoa.cast(object, AVSpeechSynthesisVoice.class);
            WrappedVoice<AVSpeechSynthesisVoice> voice = new RococoaVoice(nativeVoice);
            voiceList.add(voice);
        }
} catch (Throwable e) {
 logger.log(Logger.Level.WARNING, "this exception may be caused running on x86_64 chip", e);
}
        return voiceList;
    }

    @Override
    public String getDomain() {
        return "general";
    }

    @Override
    public Locale getLocale() {
//Debug.println("locale: " + Locale.forLanguageTag(getSpeechLocale().toString().replaceAll("_", "-")) + ", source: " + getSpeechLocale().toString());
        return Locale.forLanguageTag(getSpeechLocale().toString().replaceAll("_", "-"));
    }

    private static final Pattern localPattern = Pattern.compile("\\p{Alpha}{2}-\\p{Alpha}{2}");

    /** */
    private static SpeechLocale getSpeechLocale(AVSpeechSynthesisVoice nativeVoice) {
        try {
            Matcher m = localPattern.matcher(nativeVoice.identifier());
            if (m.find()) {
                String found = m.group();
//Debug.println("found: " + found);
                String[] pair = found.split("-");
//Debug.println("locale: " + nativeVoice.identifier() + ", " + pair[0] + ", " +  pair[1]);
                return new SpeechLocale(pair[0], pair[1], "");
            }
        } catch (Exception e) {
Debug.println(Level.FINE, "getSpeechLocale: " + e);
        }
Debug.println(Level.FINEST, "getSpeechLocale: " + nativeVoice.identifier());
        return SpeechLocale.getDefault();
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
            case AVSpeechSynthesisVoiceGenderFemale -> Voice.GENDER_FEMALE;
            case AVSpeechSynthesisVoiceGenderMale -> Voice.GENDER_MALE;
            case AVSpeechSynthesisVoiceGenderUnspecified -> Voice.GENDER_NEUTRAL;
            default -> Voice.GENDER_DONT_CARE;
        };
    }
}
