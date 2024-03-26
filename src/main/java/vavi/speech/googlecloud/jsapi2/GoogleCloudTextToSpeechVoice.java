/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlecloud.jsapi2;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;

import com.google.cloud.texttospeech.v1.ListVoicesRequest;
import com.google.cloud.texttospeech.v1.ListVoicesResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import vavi.speech.WrappedVoice;
import vavi.util.Debug;


/**
 * GoogleCloudTextToSpeechVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-14 nsano initial version <br>
 */
public class GoogleCloudTextToSpeechVoice extends WrappedVoice<com.google.cloud.texttospeech.v1.Voice> {

    /** */
    public static final GoogleCloudTextToSpeechVoice factory = new GoogleCloudTextToSpeechVoice();

    /** for factory use only */
    private GoogleCloudTextToSpeechVoice() {
        super(null);
    }

    /** */
    protected GoogleCloudTextToSpeechVoice(com.google.cloud.texttospeech.v1.Voice nativeVoice) {
        super(toSpeechLocale(nativeVoice),
                nativeVoice.getName(),
                toGender(nativeVoice),
                Voice.AGE_DONT_CARE,
                Voice.VARIANT_DONT_CARE,
                nativeVoice);
    }

    /** @throws IllegalStateException credentials are not set */
    @Override
    public List<com.google.cloud.texttospeech.v1.Voice> getAllNativeVoices() {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            ListVoicesRequest request = ListVoicesRequest.getDefaultInstance();

            ListVoicesResponse response = textToSpeechClient.listVoices(request);
            List<com.google.cloud.texttospeech.v1.Voice> voices = response.getVoicesList();

            return voices;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** @throws IllegalStateException credentials are not set */
    @Override
    public List<WrappedVoice<com.google.cloud.texttospeech.v1.Voice>> getAllVoices() {
        List<WrappedVoice<com.google.cloud.texttospeech.v1.Voice>> voiceList = new LinkedList<>();
        for (com.google.cloud.texttospeech.v1.Voice nativeVoice : getAllNativeVoices()) {
            WrappedVoice<com.google.cloud.texttospeech.v1.Voice> voice = new GoogleCloudTextToSpeechVoice(nativeVoice);
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
        return Locale.forLanguageTag(nativeVoice.getLanguageCodes(0));
    }

    /** native locale conversion */
    private static SpeechLocale toSpeechLocale(com.google.cloud.texttospeech.v1.Voice nativeVoice) {
        String[] pair = nativeVoice.getLanguageCodes(0).split("-");
        return new SpeechLocale(pair[0], pair[1], "");
    }

    /** native gender conversion */
    private static int toGender(com.google.cloud.texttospeech.v1.Voice nativeVoice) {
Debug.println(Level.FINEST, "nativeGender: " + nativeVoice.getName() + ", " + nativeVoice.getSsmlGenderValue());
        return switch (nativeVoice.getSsmlGenderValue()) {
            case 1 -> Voice.GENDER_MALE;
            case 2 -> Voice.GENDER_FEMALE;
            default -> Voice.GENDER_DONT_CARE;
        };
    }
}
