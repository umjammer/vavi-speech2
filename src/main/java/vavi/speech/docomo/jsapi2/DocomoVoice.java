/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.docomo.jsapi2;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;
import vavi.speech.voicevox.VoiceVox;

import static java.lang.System.getLogger;


/**
 * DocomoVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024/03/21 umjammer initial version <br>
 */
public class DocomoVoice extends WrappedVoice<Voice> {

    private static final Logger logger = getLogger(DocomoVoice.class.getName());

    /** */
    public static final DocomoVoice factory = new DocomoVoice();

    /** for factory use only */
    private DocomoVoice() {
        super(null);
    }

    /** */
    protected DocomoVoice(Voice nativeVoice) {
        super(nativeVoice.getSpeechLocale(),
                nativeVoice.getName(),
                nativeVoice.getGender(),
                nativeVoice.getAge(),
                nativeVoice.getVariant(),
                nativeVoice);
    }

    @Override
    public List<Voice> getAllNativeVoices() {
        return nativeVoices;
    }

    @Override
    public List<WrappedVoice<Voice>> getAllVoices() {
        List<WrappedVoice<Voice>> voices = new ArrayList<>();
        nativeVoices.stream().map(DocomoVoice::new).forEach(voices::add);
        return voices;
    }

    /** */
    private static final List<Voice> nativeVoices = new ArrayList<>();

    static {
        try (VoiceVox voiceVox = new VoiceVox()) {
            nativeVoices.addAll(Arrays.asList(voiceVox.getAllVoices()));
        } catch (Throwable e) {
            logger.log(Level.WARNING, "no Docomo server found");
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
