/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.rococoa.jsapi2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
import org.rococoa.cocoa.appkit.NSSpeechSynthesizer;
import org.rococoa.cocoa.appkit.NSVoice;

import vavi.speech.rococoa.SynthesizerDelegate;


/**
 * A Cocoa compliant {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public final class RococoaSynthesizer extends BaseSynthesizer {
    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(RococoaSynthesizer.class.getName());

    /** */
    private NSSpeechSynthesizer synthesizer;

    /** */
    private SynthesizerDelegate delegate;

    /**
     * Constructs a new synthesizer object.
     *
     * @param mode the synthesizer mode
     */
    RococoaSynthesizer(RococoaSynthesizerMode mode) {
        super(mode);
    }

    /* */
    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        Voice voice;
        RococoaSynthesizerMode mode = (RococoaSynthesizerMode) getEngineMode();
        if (mode == null) {
            voice = null;
        } else {
            Voice[] voices = mode.getVoices();
            if (voices == null) {
                voice = null;
            } else {
                Optional<Voice> result = Arrays.stream(voices).filter(v -> v.getName().equals(NSSpeechSynthesizer.defaultVoice().getName())).findFirst();
//System.err.println("default voice1: " + result.get().getName());
                voice = result.orElse(null);
            }
        }
        LOGGER.fine("default voice: " + voice.getName());
        getSynthesizerProperties().setVoice(voice);

//System.err.println("default voice2: " + NSSpeechSynthesizer.defaultVoice().getName());
        synthesizer = NSSpeechSynthesizer.synthesizerWithVoice(null);
        delegate = new SynthesizerDelegate(synthesizer);

        //
        long newState = ALLOCATED | RESUMED;
        newState |= (getQueueManager().isQueueEmpty() ? QUEUE_EMPTY : QUEUE_NOT_EMPTY);
        setEngineState(CLEAR_ALL_STATE, newState);
    }

    /** */
    private NSVoice toNativeVoice(Voice voice) {
//System.err.println("vioce2: " + getSynthesizerProperties().getVoice());
        if (voice == null) {
            return null;
        }
        Optional<NSVoice> result = NSSpeechSynthesizer.availableVoices().stream().filter(v -> v.getName().equals(voice.getName())).findFirst();
        return result.orElse(null);
    }

    @Override
    public boolean handleCancel() {
//        synthesizer.stopSpeaking();
        return false;
    }

    @Override
    protected boolean handleCancel(int id) {
//        synthesizer.stopSpeaking();
        return false;
    }

    @Override
    protected boolean handleCancelAll() {
//        synthesizer.stopSpeaking();
        return false;
    }

    @Override
    public void handleDeallocate() {
        // Leave some time to let all resources detach
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        synthesizer.release();

        //
        setEngineState(CLEAR_ALL_STATE, DEALLOCATED);
        getQueueManager().cancelAllItems();
        getQueueManager().terminate();
    }

    @Override
    public void handlePause() {
//        synthesizer.pauseSpeakingAtBoundary(NSSpeechBoundary.ImmediateBoundary);
    }

    @Override
    public boolean handleResume() {
//        synthesizer.continueSpeaking();
        return false;
    }

    @Override
    public AudioSegment handleSpeak(int id, String item) {
        AudioManager manager = getAudioManager();
        String locator = manager.getMediaLocator();
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
            synthesizer.setVoice(toNativeVoice(getSynthesizerProperties().getVoice()));
            Path path = Files.createTempFile(getClass().getName(), ".aiff");
            synthesizer.startSpeakingStringToURL(text, path.toUri());
            // wait to finish writing whole data
            delegate.waitForSpeechDone(10000, true);
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
        return new AudioFormat(22050.0f, 16, 1, true, false);
    }

    @Override
    protected void handlePropertyChangeRequest(BaseEngineProperties properties,
                                               String propName,
                                               Object oldValue,
                                               Object newValue) {
        properties.commitPropertyChange(propName, oldValue, newValue);
    }
}
