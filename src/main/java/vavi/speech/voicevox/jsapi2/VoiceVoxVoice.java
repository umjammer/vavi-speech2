/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.voicevox.jsapi2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.speech.synthesis.Voice;

import org.checkerframework.checker.units.qual.A;
import vavi.speech.WrappedVoice;
import vavi.speech.voicevox.VoiceVox;

import static java.lang.System.getLogger;


/**
 * VoiceVoxVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-14 nsano initial version <br>
 */
public class VoiceVoxVoice extends WrappedVoice<Voice> {

    private static final Logger logger = getLogger(VoiceVoxVoice.class.getName());

    /** */
    public static final VoiceVoxVoice factory = new VoiceVoxVoice();

    /** for factory use only */
    private VoiceVoxVoice() {
        super(null);
    }

    /** */
    protected VoiceVoxVoice(Voice nativeVoice) {
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
        nativeVoices.stream().map(VoiceVoxVoice::new).forEach(voices::add);
        return voices;
    }

    /** */
    private static final List<Voice> nativeVoices = new ArrayList<>();

    static {
        try (VoiceVox voiceVox = new VoiceVox()) {
            nativeVoices.addAll(Arrays.asList(voiceVox.getAllVoices()));
        } catch (IOException e) {
            logger.log(Level.WARNING, "no VoiceVox server found");
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
