/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.voicestudio.jsapi2;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;
import vavi.speech.voicestudio.VoiceStudio;

import static java.lang.System.getLogger;


/**
 * VoiceStudioVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-11 nsano initial version <br>
 */
public class VoiceStudioVoice extends WrappedVoice<VoiceStudio.Voice> {

    private static final Logger logger = getLogger(VoiceStudioVoice.class.getName());

    /** */
    public static final VoiceStudioVoice factory = new VoiceStudioVoice();

    /** for factory use only */
    private VoiceStudioVoice() {
        super(null);
    }

    /** */
    protected VoiceStudioVoice(VoiceStudio.Voice nativeVoice) {
        super(new SpeechLocale(Locale.JAPANESE.toLanguageTag()),
                nativeVoice.name,
                getGender(nativeVoice.description),
                Voice.AGE_DONT_CARE,
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    /** */
    private static int getGender(String description) {
        if (description == null) return Voice.GENDER_DONT_CARE;
        String d = description.toLowerCase();
        if (d.contains("female")) return Voice.GENDER_FEMALE;
        else if (d.contains("male")) return Voice.GENDER_MALE;
        else return Voice.GENDER_DONT_CARE;
    }

    @Override
    public List<VoiceStudio.Voice> getAllNativeVoices() {
        return nativeVoices;
    }

    @Override
    public List<WrappedVoice<VoiceStudio.Voice>> getAllVoices() {
        List<WrappedVoice<VoiceStudio.Voice>> voices = new ArrayList<>();
        nativeVoices.stream().map(VoiceStudioVoice::new).forEach(voices::add);
        return voices;
    }

    /** */
    private static final List<VoiceStudio.Voice> nativeVoices = new ArrayList<>();

    static {
        try (VoiceStudio voiceStudio = new VoiceStudio()) {
            nativeVoices.addAll(Arrays.asList(voiceStudio.getAllVoices()));
        } catch (Throwable e) {
            logger.log(Level.INFO, "no VoiceStudio server found");
            logger.log(Level.TRACE, e.getMessage(), e);
        }
    }

    @Override
    public String getDomain() {
        return "general";
    }

    @Override
    public Locale getLocale() {
        return Locale.ROOT;
    }
}
