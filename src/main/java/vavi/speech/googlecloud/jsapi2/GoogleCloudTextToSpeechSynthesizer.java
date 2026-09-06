/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlecloud.jsapi2;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
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

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import org.jvoicexml.jsapi2.BaseAudioSegment;
import org.jvoicexml.jsapi2.BaseEngineProperties;
import org.jvoicexml.jsapi2.synthesis.BaseSynthesizer;
import vavi.speech.WrappedVoice;


/**
 * A Google Cloud Text To Speech compliant {@link javax.speech.synthesis.Synthesizer}.
 * <p>
 * system property
 * <li>{@code vavi.speech.googlecloud.credential} ... path to credential default {@code google-app-credentials.json}
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public final class GoogleCloudTextToSpeechSynthesizer extends BaseSynthesizer {

    /** Logger for this class. */
    private static final Logger logger = System.getLogger(GoogleCloudTextToSpeechSynthesizer.class.getName());

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
        if (getSynthesizerProperties().getVoice() == null) {
            Voice voice;
            GoogleCloudTextToSpeechSynthesizerMode mode = (GoogleCloudTextToSpeechSynthesizerMode) getEngineMode();
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

        try {
            // https://gemini.google.com/app/e8f90bb971c64462
            String serviceAccountKeyPath = System.getProperty("vavi.speech.googlecloud.credential", "google-app-credentials.json");
logger.log(Level.DEBUG, "vavi.speech.googlecloud.credential: " + System.getProperty("vavi.speech.googlecloud.credential"));
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountKeyPath));
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            this.client = TextToSpeechClient.create(settings);
        } catch (IOException e) {
            throw (EngineException) new EngineException("real speech engine creation failed").initCause(e);
        }
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
        client.shutdownNow();
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
    public AudioSegment handleSpeak(int id, String item) {
        try {
            byte[] bytes = synthesize(item);
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
    private byte[] synthesize(String text) {
        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        @SuppressWarnings("unchecked")
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(getSynthesizerProperties().getVoice().getSpeechLocale().getLanguage())
                .setName(((WrappedVoice<com.google.cloud.texttospeech.v1.Voice>) getSynthesizerProperties().getVoice()).getNativeVoice().getName())
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
