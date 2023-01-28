/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.openjtalk.jsapi2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
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

import vavi.speech.openjtalk.OpenJTalkWrapper;
import vavi.speech.openjtalk.OpenJTalkWrapper.VoiceFileInfo;


/**
 * A OpenJTalk compliant {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/26 umjammer initial version <br>
 */
public final class OpenJTalkSynthesizer extends BaseSynthesizer {
    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(OpenJTalkSynthesizer.class.getName());

    /** */
    private OpenJTalkWrapper openJTalk = new OpenJTalkWrapper();

    /**
     * Constructs a new synthesizer object.
     *
     * @param mode the synthesizer mode
     */
    OpenJTalkSynthesizer(OpenJTalkSynthesizerMode mode) {
        super(mode);
    }

    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        Voice voice;
        OpenJTalkSynthesizerMode mode = (OpenJTalkSynthesizerMode) getEngineMode();
        if (mode == null) {
            voice = null;
        } else {
            Voice[] voices = mode.getVoices();
            if (voices == null || voices.length < 1) {
                voice = null;
            } else {
                voice = voices[0];
            }
        }
        LOGGER.fine("default voice: " + (voice != null ? voice.getName() : ""));
        getSynthesizerProperties().setVoice(voice);

        //
        long newState = ALLOCATED | RESUMED;
        newState |= (getQueueManager().isQueueEmpty() ? QUEUE_EMPTY : QUEUE_NOT_EMPTY);
        setEngineState(CLEAR_ALL_STATE, newState);
    }

    /** */
    private VoiceFileInfo toNativeVoice(Voice voice) {
        Optional<VoiceFileInfo> result = openJTalk.getVoices().stream().filter(v -> v.name.equals(voice != null ? voice.getName() : null)).findFirst();
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
        openJTalk = null;

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
        AudioManager manager = getAudioManager();
        String locator = manager.getMediaLocator();
        // you should pass bytes to BaseAudioSegment as AudioInputStream or causes crackling!
        InputStream in = synthe(item);
        AudioSegment segment;
        if (locator == null) {
            segment = new BaseAudioSegment(item, in);
        } else {
            segment = new BaseAudioSegment(locator, item, in);
        }
        return segment;
    }

    /** */
    private AudioInputStream synthe(String text) {
        try {
//System.err.println("vioce: " + getSynthesizerProperties().getVoice());
            openJTalk.setVoice(toNativeVoice(getSynthesizerProperties().getVoice()));
            Path path = Files.createTempFile(getClass().getName(), ".aiff");
            openJTalk.speakToFile(text, path.toString());
            byte[] wav = Files.readAllBytes(path);
            ByteArrayInputStream bais = new ByteArrayInputStream(wav);
            // you should pass bytes to BaseAudioSegment as AudioInputStream or causes crackling!
            AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
            Files.delete(path);
            return ais;
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
