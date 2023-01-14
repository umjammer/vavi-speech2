/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlecloud.jsapi2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.speech.AudioException;
import javax.speech.AudioManager;
import javax.speech.AudioSegment;
import javax.speech.EngineException;
import javax.speech.EngineStateException;
import javax.speech.synthesis.Speakable;
import javax.speech.synthesis.Voice;

import org.jvoicexml.jsapi2.BaseAudioSegment;
import org.jvoicexml.jsapi2.BaseEngineProperties;
import org.jvoicexml.jsapi2.synthesis.BaseSynthesizer;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;


/**
 * A Cocoa compliant {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public final class GoogleCloudTextToSpeechSynthesizer extends BaseSynthesizer {
    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(GoogleCloudTextToSpeechSynthesizer.class.getName());

    /** */
    private TextToSpeechClient client;

    /**
     * Constructs a new synthesizer object.
     *
     * @param mode the synthesizer mode
     */
    GoogleCloudTextToSpeechSynthesizer(GoogleCloudTextToSpeechSynthesizerMode mode) {
        super(mode);
    }

    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        Voice voice;
        GoogleCloudTextToSpeechSynthesizerMode mode = (GoogleCloudTextToSpeechSynthesizerMode) getEngineMode();
        if (mode == null) {
            voice = null;
        } else {
            Voice[] voices = mode.getVoices();
            if (voices == null) {
                voice = null;
            } else {
                voice = voices[0];
            }
        }
LOGGER.fine("default voice: " + voice.getName());
        getSynthesizerProperties().setVoice(voice);

        try {
            this.client = TextToSpeechClient.create();
        } catch (IOException e) {
            throw (EngineException) new EngineException().initCause(e);
        }
    }

    /** */
    private com.google.cloud.texttospeech.v1.Voice toNativeVoice(Voice voice) {
        if (voice == null) {
            return null;
        }
        Optional<com.google.cloud.texttospeech.v1.Voice> result = GoogleCloudEngineListFactory.listAllSupportedVoices().stream().filter(v -> v.getName().equals(voice.getName())).findFirst();
        return result.orElse(null);
    }

    @Override
    public boolean handleCancel() {
        return false;
    }

    @Override
    protected boolean handleCancel(int id) {
        return false;
    }

    @Override
    protected boolean handleCancelAll() {
        return false;
    }

    @Override
    public void handleDeallocate() {
        // Leave some time to let all resources detach
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        client.close();
    }

    @Override
    public void handlePause() {
    }

    @Override
    public boolean handleResume() {
        return false;
    }

    @Override
    public AudioSegment handleSpeak(int id, String item) {
        try {
            byte[] bytes = synthe(item);
            AudioManager manager = getAudioManager();
            String locator = manager.getMediaLocator();
            // you should pass bytes to BaseAudioSegment as AudioInputStream or causes crackling!
            InputStream in = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes));
            AudioSegment segment;
            if (locator == null) {
                segment = new BaseAudioSegment(item, in);
            } else {
                segment = new BaseAudioSegment(locator, item, in);
            }
            return segment;
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException(e);
        }
    }

    /** */
    private byte[] synthe(String text) {
        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(getSynthesizerProperties().getVoice().getSpeechLocale().getLanguage())
                .setName(toNativeVoice(getSynthesizerProperties().getVoice()).getName())
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.LINEAR16)
                .build();

        SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

        ByteString audioContents = response.getAudioContent();
        return audioContents.toByteArray();
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
