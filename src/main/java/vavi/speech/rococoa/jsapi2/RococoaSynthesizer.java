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
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
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
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSObject;
import vavi.speech.rococoa.SynthesizerDelegate;
import vavi.util.Debug;
import vavix.rococoa.avfoundation.AVSpeechSynthesisVoice;
import vavix.rococoa.avfoundation.AVSpeechSynthesizer;
import vavix.rococoa.avfoundation.AVSpeechUtterance;


/**
 * A Cocoa compliant {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public final class RococoaSynthesizer extends BaseSynthesizer {

    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(RococoaSynthesizer.class.getName());

    /** */
    private AVSpeechSynthesizer synthesizer;

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

    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        Voice voice;
        RococoaSynthesizerMode mode = (RococoaSynthesizerMode) getEngineMode();
        if (mode != null) {
            Voice[] voices = mode.getVoices();
            if (voices != null) {
                try {
                    AVSpeechSynthesisVoice defaultNativeVoice = AVSpeechSynthesisVoice.withLanguage(Locale.getDefault().toString());
                    Optional<Voice> result = Arrays.stream(voices).filter(v -> v.getName().equals(defaultNativeVoice.name())).findFirst();
//Debug.println(Level.FINER, "default voice1: " + result.get().getName());
                    if (result.isPresent()) {
                        voice = result.get();
logger.fine("default voice: " + voice.getName());
                        getSynthesizerProperties().setVoice(voice);
                    }
                } catch (IllegalArgumentException e) {
Debug.println(Level.FINE, "no default voice");
                }
            }
        }

//Debug.println(Level.FINER, "default voice2: " + NSSpeechSynthesizer.defaultVoice().getName());
        synthesizer = AVSpeechSynthesizer.newInstance();
        delegate = new SynthesizerDelegate(synthesizer); // delegate is implemented in vavi-speech

        //
        long newState = ALLOCATED | RESUMED;
        newState |= (getQueueManager().isQueueEmpty() ? QUEUE_EMPTY : QUEUE_NOT_EMPTY);
        setEngineState(CLEAR_ALL_STATE, newState);
    }

    /** */
    private AVSpeechSynthesisVoice toNativeVoice(Voice voice) {
Debug.println(Level.FINER, "toNativeVoice: " + getSynthesizerProperties().getVoice());
        if (voice == null) {
            return null;
        }
        for (NSObject object : AVSpeechSynthesisVoice.speechVoices()) {
            AVSpeechSynthesisVoice nativeVoice = Rococoa.cast(object, AVSpeechSynthesisVoice.class);
            if (nativeVoice.name().equals(voice.getName())) {
Debug.println(Level.FINER, "result: " + nativeVoice);
                return nativeVoice;
            }
        }
        return null;
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
        if (false) {
            AudioManager manager = getAudioManager();
            String locator = manager.getMediaLocator();
            InputStream in = synthesis(item);
            AudioSegment segment;
            if (locator == null) {
                segment = new BaseAudioSegment(item, in);
            } else {
                segment = new BaseAudioSegment(locator, item, in);
            }
            return segment;
        } else {
try { // TODO ad-hoc
            AVSpeechUtterance utterance = AVSpeechUtterance.of(item);
            utterance.setVoice(toNativeVoice(getSynthesizerProperties().getVoice()));
            utterance.setVolume(getSynthesizerProperties().getVolume() / 100f);
            synthesizer.speakUtterance(utterance);
            delegate.waitForSpeechDone(10000, true);
            return new BaseAudioSegment(item, AudioSystem.getAudioInputStream(RococoaSynthesizer.class.getResourceAsStream("/zero.wav")));
} catch (Throwable t) {
 Debug.printStackTrace(t);
 return null;
}
        }
    }

    /** */
    private AudioInputStream synthesis(String text) {
        try {
//Debug.println(Level.FINER, "voice: " + getSynthesizerProperties().getVoice());
            AVSpeechUtterance utterance = AVSpeechUtterance.of(text);
            utterance.setVoice(toNativeVoice(getSynthesizerProperties().getVoice()));
            Path path = Files.createTempFile(getClass().getName(), ".aiff");
            synthesizer.writeUtterance_toBufferCallback(utterance, null);
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
