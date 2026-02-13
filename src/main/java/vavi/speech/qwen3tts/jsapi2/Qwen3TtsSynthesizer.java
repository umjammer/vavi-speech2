/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.qwen3tts.jsapi2;

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
import vavi.speech.qwen3tts.Qwen3Tts;


/**
 * A Qwen3-TTS {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2026/02/13 umjammer initial version <br>
 */
public final class Qwen3TtsSynthesizer extends BaseSynthesizer {

    /** Logger for this class. */
    private static final Logger logger = System.getLogger(Qwen3TtsSynthesizer.class.getName());

    /** */
    private Qwen3Tts client;

    /**
     * Constructs a new synthesizer object.
     *
     * @param mode the synthesizer mode
     */
    Qwen3TtsSynthesizer(Qwen3TtsSynthesizerMode mode) {
        super(mode);
    }

    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        if (getSynthesizerProperties().getVoice() == null) {
            Voice voice;
            Qwen3TtsSynthesizerMode mode = (Qwen3TtsSynthesizerMode) getEngineMode();
            if (mode == null) {
                throw new EngineException("not engine mode");
            } else {
                Voice[] voices = mode.getVoices();
                if (voices == null || voices.length < 1) {
                    throw new EngineException("no voice");
                } else if (voices.length > 6) {
                    voice = voices[6];
                } else {
logger.log(Level.WARNING, "too few default voices: " + voices.length);
                    voice = voices[0];
                }
            }
logger.log(Level.DEBUG, "default voice: " + voice.getName());
            getSynthesizerProperties().setVoice(voice);
        }

        try {
            this.client = new Qwen3Tts();
            this.client.getAllVoices(); // necessary
        } catch (IllegalStateException e) {
            throw (EngineException) new EngineException().initCause(e.getCause());
        }

        //
        long newState = ALLOCATED | RESUMED;
        newState |= (getQueueManager().isQueueEmpty() ? QUEUE_EMPTY : QUEUE_NOT_EMPTY);
        setEngineState(CLEAR_ALL_STATE, newState);
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
        setEngineState(CLEAR_ALL_STATE, DEALLOCATED);
        getQueueManager().cancelAllItems();
        getQueueManager().terminate();
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
            SynthesizerProperties props = getSynthesizerProperties();
            // TODO adapt parameters
            InputStream wave = client.synthesize(item, props.getVoice().getName());
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
