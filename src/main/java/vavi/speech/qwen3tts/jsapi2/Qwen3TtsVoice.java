/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.qwen3tts.jsapi2;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;
import vavi.speech.qwen3tts.Qwen3Tts;

import static java.lang.System.getLogger;


/**
 * QwenTtsVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-02-13 nsano initial version <br>
 */
public class Qwen3TtsVoice extends WrappedVoice<Qwen3Tts.Voice> {

    private static final Logger logger = getLogger(Qwen3TtsVoice.class.getName());

    /** */
    public static final Qwen3TtsVoice factory = new Qwen3TtsVoice();

    /** for factory use only */
    private Qwen3TtsVoice() {
        super(null);
    }

    /** */
    protected Qwen3TtsVoice(Qwen3Tts.Voice nativeVoice) {
        super(new SpeechLocale(Locale.JAPANESE.toLanguageTag()),
                nativeVoice.name,
                getGender(nativeVoice.description),
                Voice.AGE_DONT_CARE,
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    /** */
    private static int getGender(String gender) {
        if (gender.toLowerCase().contains("male")) return Voice.GENDER_MALE;
        else if (gender.toLowerCase().contains("female")) return Voice.GENDER_FEMALE;
        else return Voice.GENDER_DONT_CARE;
    }

    @Override
    public List<Qwen3Tts.Voice> getAllNativeVoices() {
        return nativeVoices;
    }

    @Override
    public List<WrappedVoice<Qwen3Tts.Voice>> getAllVoices() {
        List<WrappedVoice<Qwen3Tts.Voice>> voices = new ArrayList<>();
        nativeVoices.stream().map(Qwen3TtsVoice::new).forEach(voices::add);
        return voices;
    }

    /** */
    private static final List<Qwen3Tts.Voice> nativeVoices = new ArrayList<>();

    static {
        try (Qwen3Tts aivis = new Qwen3Tts()) {
            nativeVoices.addAll(Arrays.asList(aivis.getAllVoices()));
        } catch (Throwable e) {
            logger.log(Level.INFO, "no Qwen3-TTS server found");
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
