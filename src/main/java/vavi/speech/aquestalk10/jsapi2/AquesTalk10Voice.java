/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.aquestalk10.jsapi2;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;
import vavi.speech.aquestalk10.jna.AquesTalk10.AQTK_VOICE;
import vavi.speech.aquestalk10.jna.AquesTalk10Wrapper;
import vavi.util.Debug;


/**
 * AquesTalk10Voice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-14 nsano initial version <br>
 */
public class AquesTalk10Voice extends WrappedVoice<AQTK_VOICE> {

    /** */
    public static final AquesTalk10Voice factory = new AquesTalk10Voice();

    /** for factory use only */
    private AquesTalk10Voice() {
        super(null);
    }

    /** */
    protected AquesTalk10Voice(Map.Entry<String, AQTK_VOICE> nativeVoice) {
        super(new SpeechLocale(Locale.JAPAN.toString()),
                nativeVoice.getKey(),
                toGender(nativeVoice.getValue()),
                Voice.AGE_DONT_CARE,
                Voice.VARIANT_DONT_CARE,
                nativeVoice.getValue());
    }

    @Override
    public List<AQTK_VOICE> getAllNativeVoices() {
        return AquesTalk10Wrapper.voices.values().stream().toList();
    }

    @Override
    public List<WrappedVoice<AQTK_VOICE>> getAllVoices() {
        try {
            List<WrappedVoice<AQTK_VOICE>> voices = new LinkedList<>();
            for (Map.Entry<String, AQTK_VOICE> nativeVoice : AquesTalk10Wrapper.voices.entrySet()) {
                WrappedVoice<AQTK_VOICE> voice = new AquesTalk10Voice(nativeVoice);
                voices.add(voice);
            }
            return voices;
        } catch (UnsatisfiedLinkError e) {
            if (System.getProperty("os.arch").equals("aarch64")) {
Debug.println(Level.WARNING, "AquesTalk10 doesn't support arm64 architecture.");
            } else {
Debug.println(Level.SEVERE, "install AquesTalk10 and locate frameworks at proper directory.");
            }
Debug.printStackTrace(Level.FINEST, e);
            return Collections.emptyList();
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

    /** native gender conversion */
    private static int toGender(AQTK_VOICE nativeVoice) {
        return switch (nativeVoice.bas) {
            case 0, 1 -> Voice.GENDER_FEMALE;
            case 2 -> Voice.GENDER_MALE;
            default -> Voice.GENDER_DONT_CARE;
        };
    }
}
