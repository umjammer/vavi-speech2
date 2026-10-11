/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.voicestudio.jsapi2;

import java.io.BufferedInputStream;
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
import javax.speech.synthesis.SynthesizerProperties;
import javax.speech.synthesis.Voice;

import org.jvoicexml.jsapi2.BaseAudioSegment;
import org.jvoicexml.jsapi2.BaseEngineProperties;
import org.jvoicexml.jsapi2.synthesis.BaseSynthesizer;
import vavi.speech.WrappedVoice;
import vavi.speech.voicestudio.VoiceStudio;


/**
 * A VoiceStudio {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2026/10/11 umjammer initial version <br>
 */
public final class VoiceStudioSynthesizer extends BaseSynthesizer {

    /** Logger for this class. */
    private static final Logger logger = System.getLogger(VoiceStudioSynthesizer.class.getName());

    /** */
    private VoiceStudio client;

    /**
     * Constructs a new synthesizer object.
     *
     * @param mode the synthesizer mode
     */
    VoiceStudioSynthesizer(VoiceStudioSynthesizerMode mode) {
        super(mode);
    }

    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        if (getSynthesizerProperties().getVoice() == null) {
            Voice voice;
            VoiceStudioSynthesizerMode mode = (VoiceStudioSynthesizerMode) getEngineMode();
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
            this.client = new VoiceStudio();
            this.client.getAllVoices(); // necessary
        } catch (IllegalStateException e) {
            throw (EngineException) new EngineException().initCause(e.getCause());
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
        try {
            client.close();
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
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
            SynthesizerProperties props = getSynthesizerProperties();
            // TODO adapt parameters
            @SuppressWarnings("unchecked")
            String voiceId = ((WrappedVoice<VoiceStudio.Voice>) props.getVoice()).getNativeVoice().voice_id;
            InputStream wave = client.synthesize(item, voiceId);
            AudioManager manager = getAudioManager();
            String locator = manager.getMediaLocator();
            // you should pass bytes to BaseAudioSegment as AudioInputStream or causes crackling!
            InputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(wave));
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
