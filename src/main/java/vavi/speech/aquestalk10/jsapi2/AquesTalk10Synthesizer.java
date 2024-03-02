/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.aquestalk10.jsapi2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

import vavi.beans.InstanciationBinder;
import vavi.speech.Phonemizer;
import vavi.speech.aquestalk10.jna.AquesTalk10.AQTK_VOICE;
import vavi.speech.aquestalk10.jna.AquesTalk10Wrapper;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * A AquesTalk10 compliant {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
@PropsEntity(url = "classpath:aquestalk10.properties")
public final class AquesTalk10Synthesizer extends BaseSynthesizer {

    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(AquesTalk10Synthesizer.class.getName());

    /** */
    @Property(binder = InstanciationBinder.class, value = "vavi.speech.phoneme.KuromojiJaPhonemer")
    private Phonemizer phonemizer;

    /** engine */
    private AquesTalk10Wrapper aquesTalk10;

    /**
     * Constructs a new synthesizer object.
     *
     * @param mode the synthesizer mode
     */
    AquesTalk10Synthesizer(AquesTalk10SynthesizerMode mode) {
        super(mode);
        try {
            PropsEntity.Util.bind(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        Voice voice;
        AquesTalk10SynthesizerMode mode = (AquesTalk10SynthesizerMode) getEngineMode();
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
logger.fine("default voice: " + voice.getName());
        getSynthesizerProperties().setVoice(voice);

        aquesTalk10 = AquesTalk10Wrapper.getInstance();

        //
        long newState = ALLOCATED | RESUMED;
        newState |= (getQueueManager().isQueueEmpty() ? QUEUE_EMPTY : QUEUE_NOT_EMPTY);
        setEngineState(CLEAR_ALL_STATE, newState);
    }

    /**
     * @param voice when null, returns default voice.
     */
    private static AQTK_VOICE toNativeVoice(Voice voice) {
        return AquesTalk10Wrapper.voices.get(voice == null ? "F1" : voice.getName());
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
        } catch (InterruptedException ignored) {
        }
        aquesTalk10 = null;

        //
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
            aquesTalk10.setVoice(toNativeVoice(getSynthesizerProperties().getVoice()));
            byte[] bytes = aquesTalk10.synthe(phonemizer.phoneme(item));
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

    @Override
    protected AudioSegment handleSpeak(int id, Speakable item) {
        throw new IllegalArgumentException("Synthesizer does not support" + " speech markup!");
    }

    @Override
    protected AudioFormat getEngineAudioFormat() {
        return new AudioFormat(16000.0f, 16, 1, true, false);
    }

    @Override
    protected void handlePropertyChangeRequest(BaseEngineProperties properties,
                                               String propName,
                                               Object oldValue,
                                               Object newValue) {
        properties.commitPropertyChange(propName, oldValue, newValue);
    }
}
