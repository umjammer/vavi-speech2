/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.aivis.jsapi2;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;
import vavi.speech.aivis.Aivis;
import vavi.speech.aivis.Aivis.AivisSpeaker;

import static java.lang.System.getLogger;


/**
 * AivisVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-12-14 nsano initial version <br>
 */
public class AivisVoice extends WrappedVoice<AivisSpeaker> {

    private static final Logger logger = getLogger(AivisVoice.class.getName());

    /** */
    public static final AivisVoice factory = new AivisVoice();

    /** for factory use only */
    private AivisVoice() {
        super(null);
    }

    /** */
    protected AivisVoice(AivisSpeaker nativeVoice) {
        super(new SpeechLocale(Locale.JAPANESE.toLanguageTag()),
                nativeVoice.name,
                Voice.GENDER_DONT_CARE,
                Voice.AGE_DONT_CARE,
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    @Override
    public List<AivisSpeaker> getAllNativeVoices() {
        return nativeVoices;
    }

    @Override
    public List<WrappedVoice<AivisSpeaker>> getAllVoices() {
        List<WrappedVoice<AivisSpeaker>> voices = new ArrayList<>();
        nativeVoices.stream().map(AivisVoice::new).forEach(voices::add);
        return voices;
    }

    /** */
    private static final List<AivisSpeaker> nativeVoices = new ArrayList<>();

    static {
        try (Aivis aivis = new Aivis()) {
            nativeVoices.addAll(Arrays.asList(aivis.getAllVoices()));
        } catch (Throwable e) {
            logger.log(Level.WARNING, "no Aivis server found");
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
}
