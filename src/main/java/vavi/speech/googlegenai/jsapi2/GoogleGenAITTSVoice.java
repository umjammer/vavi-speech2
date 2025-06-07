/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlegenai.jsapi2;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;

import static java.lang.System.getLogger;


/**
 * GoogleGenAITTSVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-06-08 nsano initial version <br>
 */
public class GoogleGenAITTSVoice extends WrappedVoice<String> {

    private static final Logger logger = getLogger(GoogleGenAITTSVoice.class.getName());

    /** */
    public static final GoogleGenAITTSVoice factory = new GoogleGenAITTSVoice();

    /** for factory use only */
    private GoogleGenAITTSVoice() {
        super(null);
    }

    /** */
    protected GoogleGenAITTSVoice(String nativeVoice) {
        super(SpeechLocale.getDefault(),
                nativeVoice,
                Voice.GENDER_DONT_CARE,
                Voice.AGE_DONT_CARE,
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    /** @throws IllegalStateException credentials are not set */
    @Override
    public List<String> getAllNativeVoices() {
        return voiceData;
    }

    /** @throws IllegalStateException credentials are not set */
    @Override
    public List<WrappedVoice<String>> getAllVoices() {
        List<WrappedVoice<String>> voiceList = new LinkedList<>();
        for (String nativeVoice : getAllNativeVoices()) {
            WrappedVoice<String> voice = new GoogleGenAITTSVoice(nativeVoice);
            voiceList.add(voice);
        }
        return voiceList;
    }

    @Override
    public String getDomain() {
        return "general";
    }

    @Override
    public Locale getLocale() {
        return Locale.ROOT;
    }

    /** Google GenAI voice names */
    private static final List<String> voiceData = new ArrayList<>();

    /* cvs: name, style */
    static {
        Scanner scanner = new Scanner(GoogleGenAITTSVoice.class.getResourceAsStream("/vavi/speech/googlegenai/googlegenai.csv"));
        while (scanner.hasNextLine()) {
            String[] parts = scanner.nextLine().split(",");
            String name = parts[0];
            voiceData.add(name);
        }
logger.log(Level.DEBUG, "voices: " + voiceData.size());
    }
}
