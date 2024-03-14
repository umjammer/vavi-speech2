/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.openjtalk.jsapi2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import vavi.speech.WrappedVoice;
import vavi.speech.openjtalk.OpenJTalkWrapper;
import vavi.speech.openjtalk.OpenJTalkWrapper.VoiceFileInfo;


/**
 * OpenJTalkVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-14 nsano initial version <br>
 */
public class OpenJTalkVoice extends WrappedVoice<VoiceFileInfo> {

    /** */
    public static final OpenJTalkVoice factory = new OpenJTalkVoice();

    /** */
    private static final OpenJTalkWrapper openJTalk = new OpenJTalkWrapper();

    /** for factory use only */
    private OpenJTalkVoice() {
        super(null);
    }

    /** */
    protected OpenJTalkVoice(VoiceFileInfo nativeVoice) {
        super(new SpeechLocale(Locale.JAPAN.toString()),
                nativeVoice.name,
                Voice.GENDER_DONT_CARE,
                Voice.AGE_DONT_CARE,
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    @Override
    public List<VoiceFileInfo> getAllNativeVoices() {
        return openJTalk.getVoices();
    }

    @Override
    public List<WrappedVoice<VoiceFileInfo>> getAllVoices() {
        List<WrappedVoice<VoiceFileInfo>> voices = new ArrayList<>();
        for (VoiceFileInfo nativeVoice : getAllNativeVoices()) {
            WrappedVoice<VoiceFileInfo> voice = new OpenJTalkVoice(nativeVoice);
            voices.add(voice);
        }
        return voices;
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
