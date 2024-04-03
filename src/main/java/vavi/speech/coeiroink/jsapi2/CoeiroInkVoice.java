/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.coeiroink.jsapi2;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;
import vavi.speech.coeiroink.CoeiroInk;
import vavi.speech.coeiroink.CoeiroInk.Speaker;

import static java.lang.System.getLogger;


/**
 * CoeiroInkVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-04-03 nsano initial version <br>
 */
public class CoeiroInkVoice extends WrappedVoice<Speaker> {

    private static final Logger logger = getLogger(CoeiroInkVoice.class.getName());

    /** */
    public static final CoeiroInkVoice factory = new CoeiroInkVoice();

    /** for factory use only */
    private CoeiroInkVoice() {
        super(null);
    }

    /** */
    protected CoeiroInkVoice(Speaker nativeVoice) {
        super(new SpeechLocale(Locale.JAPANESE.toLanguageTag()),
                nativeVoice.speakerName,
                Voice.GENDER_DONT_CARE,
                Voice.AGE_DONT_CARE,
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    @Override
    public List<Speaker> getAllNativeVoices() {
        return nativeVoices;
    }

    @Override
    public List<WrappedVoice<Speaker>> getAllVoices() {
        List<WrappedVoice<Speaker>> voices = new ArrayList<>();
        nativeVoices.stream().map(CoeiroInkVoice::new).forEach(voices::add);
        return voices;
    }

    /** */
    private static final List<Speaker> nativeVoices = new ArrayList<>();

    static {
        try (CoeiroInk coeiroInk = new CoeiroInk()) {
            nativeVoices.addAll(Arrays.asList(coeiroInk.getAllVoices()));
        } catch (Throwable e) {
            logger.log(Level.WARNING, "no CoeiroInk server found");
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
