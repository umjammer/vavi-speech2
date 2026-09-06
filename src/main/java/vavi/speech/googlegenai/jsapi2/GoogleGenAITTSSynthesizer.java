/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlegenai.jsapi2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.NoSuchElementException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.speech.AudioException;
import javax.speech.AudioManager;
import javax.speech.AudioSegment;
import javax.speech.EngineException;
import javax.speech.EngineStateException;
import javax.speech.synthesis.Speakable;
import javax.speech.synthesis.SpeakableException;
import javax.speech.synthesis.Voice;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.PrebuiltVoiceConfig;
import com.google.genai.types.SpeechConfig;
import com.google.genai.types.VoiceConfig;
import org.jvoicexml.jsapi2.BaseAudioSegment;
import org.jvoicexml.jsapi2.BaseEngineProperties;
import org.jvoicexml.jsapi2.synthesis.BaseSynthesizer;
import vavi.speech.WrappedVoice;


/**
 * A Google GenAI Text To Speech compliant {@link javax.speech.synthesis.Synthesizer}.
 * <p>
 * system property
 * <ol>
 *  <li>{@code vavi.speech.googlegenai.model} ... model name, default is {@code gemini-2.5-flash-preview-tts} </li>
 * </ol>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/06/08 umjammer initial version <br>
 */
public final class GoogleGenAITTSSynthesizer extends BaseSynthesizer {

    /** Logger for this class. */
    private static final Logger logger = System.getLogger(GoogleGenAITTSSynthesizer.class.getName());

    private final String model = System.getProperty("vavi.speech.googlegenai.model", "gemini-2.5-flash-preview-tts");

    /** */
    private Client client;

    /**
     * Constructs a new synthesizer object.
     *
     * @param mode the synthesizer mode
     */
    GoogleGenAITTSSynthesizer(GoogleGenAITTSSynthesizerMode mode) {
        super(mode);
    }

    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        if (getSynthesizerProperties().getVoice() == null) {
            Voice voice;
            GoogleGenAITTSSynthesizerMode mode = (GoogleGenAITTSSynthesizerMode) getEngineMode();
            if (mode == null) {
                throw new EngineException("not engine mode");
            } else {
                Voice[] voices = mode.getVoices();
                if (voices == null || voices.length < 1) {
                    throw new EngineException("no voice");
                } else {
                    voice = voices[0];
                }
            }
logger.log(Level.DEBUG, "default voice: " + voice.getName());
            getSynthesizerProperties().setVoice(voice);
        }

        this.client = new Client();
    }

    @Override
    public boolean handleCancel() {
        return true;
    }

    @Override
    protected boolean handleCancel(int id) {
        return true;
    }

    @Override
    protected boolean handleCancelAll() {
        return true;
    }

    @Override
    public void handleDeallocate() {
        client.close();
    }

    @Override
    public void handlePause() {
    }

    @Override
    public boolean handleResume() {
        return true;
    }

    @Override
    public AudioSegment handleSpeak(int id, String item) throws SpeakableException {
        try {
            byte[] bytes = synthesize(item);
            AudioManager manager = getAudioManager();
            String locator = manager.getMediaLocator();
            // you should pass bytes to BaseAudioSegment as AudioInputStream or causes crackling!
            InputStream in = new AudioInputStream(new ByteArrayInputStream(bytes), getEngineAudioFormat(), -1);
            AudioSegment segment;
            if (locator == null) {
                segment = new BaseAudioSegment(item, in);
            } else {
                segment = new BaseAudioSegment(locator, item, in);
            }
            return segment;
        } catch (NoSuchElementException e) {
            throw (SpeakableException) new SpeakableException().initCause(e);
        }
    }

    /** */
    private byte[] synthesize(String text) {
        @SuppressWarnings("unchecked")
        String name = ((WrappedVoice<String>) getSynthesizerProperties().getVoice()).getNativeVoice();
logger.log(Level.DEBUG, "model: " + model);
logger.log(Level.DEBUG, "voice: " + name);
logger.log(Level.DEBUG, "text: " + text);
        GenerateContentResponse response =
                client.models.generateContent(
                        model, // TODO make it property
                        text,
                        GenerateContentConfig.builder()
                                .responseModalities(List.of("AUDIO"))
                                .speechConfig(SpeechConfig.builder()
                                        .voiceConfig(VoiceConfig.builder()
                                                .prebuiltVoiceConfig(PrebuiltVoiceConfig.builder()
                                                        .voiceName(name)
                                                        .build())
                                                .build())
                                        .build())
                                .build());

        byte[] data = response
                .candidates().orElseThrow().getFirst()
                .content().orElseThrow()
                .parts().orElseThrow().getFirst()
                .inlineData().orElseThrow()
                .data().orElseThrow();
logger.log(Level.DEBUG, "data: " + data.length);
        return data;
    }

    @Override
    protected AudioSegment handleSpeak(int id, Speakable item) {
        throw new IllegalArgumentException("Synthesizer does not support" + " speech markup!");
    }

    @Override
    protected AudioFormat getEngineAudioFormat() {
        return new AudioFormat(24000.0f, 16, 1, true, false);
    }

    @Override
    protected void handlePropertyChangeRequest(BaseEngineProperties properties,
                                               String propName,
                                               Object oldValue,
                                               Object newValue) {
        properties.commitPropertyChange(propName, oldValue, newValue);
    }
}
