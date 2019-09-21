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
import org.rococoa.contrib.appkit.NSSpeechSynthesizer;
import org.rococoa.contrib.appkit.NSVoice;

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
    RococoaSynthesizer(final RococoaSynthesizerMode mode) {
        super(mode);
    }

    /* */
    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        final Voice voice;
        final RococoaSynthesizerMode mode = (RococoaSynthesizerMode) getEngineMode();
        if (mode == null) {
            voice = null;
        } else {
            final Voice[] voices = mode.getVoices();
            if (voices == null) {
                voice = null;
            } else {
                Optional<Voice> result = Arrays.asList(voices).stream().filter(v -> v.getName().equals(NSSpeechSynthesizer.defaultVoice().getName())).findFirst();
//System.err.println("default voice1: " + result.get().getName());
                voice = result.isPresent() ? result.get() : null;
            }
        }
        LOGGER.fine("default voice: " + voice.getName());
        getSynthesizerProperties().setVoice(voice);

//System.err.println("default voice2: " + NSSpeechSynthesizer.defaultVoice().getName());
        synthesizer = NSSpeechSynthesizer.synthesizerWithVoice(null);
        delegate = new SynthesizerDelegate(synthesizer);
    }

    /** */
    private NSVoice toNativeVoice(Voice voice) {
//System.err.println("vioce2: " + getSynthesizerProperties().getVoice());
        if (voice == null) {
            return null;
        }
        Optional<NSVoice> result = NSSpeechSynthesizer.availableVoices().stream().filter(v -> v.getName().equals(voice.getName())).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    /* */
    @Override
    public boolean handleCancel() {
//        synthesizer.stopSpeaking();
        return false;
    }

    /* */
    @Override
    protected boolean handleCancel(final int id) {
//        synthesizer.stopSpeaking();
        return false;
    }

    /* */
    @Override
    protected boolean handleCancelAll() {
//        synthesizer.stopSpeaking();
        return false;
    }

    /* */
    @Override
    public void handleDeallocate() {
        // Leave some time to let all resources detach
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        synthesizer.release();
    }

    /* */
    @Override
    public void handlePause() {
//        synthesizer.pauseSpeakingAtBoundary(NSSpeechBoundary.ImmediateBoundary);
    }

    /* */
    @Override
    public boolean handleResume() {
//        synthesizer.continueSpeaking();
        return false;
    }

    /* */
    @Override
    public AudioSegment handleSpeak(final int id, final String item) {
        final AudioManager manager = getAudioManager();
        final String locator = manager.getMediaLocator();
        final InputStream in = synthe(item);
        final AudioSegment segment;
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
            // you should pass bytes to BaseAudioSegment as AudioInputStream nor causes crackling!
            AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
            Files.delete(path);
            return ais;
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new IllegalStateException(e);
        }
    }

    /* */
    @Override
    protected AudioSegment handleSpeak(final int id, final Speakable item) {
        throw new IllegalArgumentException("Synthesizer does not support" + " speech markup!");
    }

    /* */
    @Override
    protected AudioFormat getEngineAudioFormat() {
        return new AudioFormat(22050.0f, 16, 1, true, false);
    }

    /* */
    @Override
    protected void handlePropertyChangeRequest(final BaseEngineProperties properties,
                                               final String propName,
                                               final Object oldValue,
                                               final Object newValue) {
        properties.commitPropertyChange(propName, oldValue, newValue);
    }
}
