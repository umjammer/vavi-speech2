/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.gyutan.jsapi2;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
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

import org.icn.gyutan.Gyutan;
import org.jvoicexml.jsapi2.BaseAudioSegment;
import org.jvoicexml.jsapi2.BaseEngineProperties;
import org.jvoicexml.jsapi2.synthesis.BaseSynthesizer;


/**
 * A Gyutan compliant {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/01/20 umjammer initial version <br>
 */
public final class GyutanSynthesizer extends BaseSynthesizer {

    /** Logger for this class. */
    private static final Logger logger = System.getLogger(GyutanSynthesizer.class.getName());

    /**
     * Constructs a new synthesizer object.
     *
     * @param mode the synthesizer mode
     */
    GyutanSynthesizer(GyutanSynthesizerMode mode) {
        super(mode);
    }

    @Override
    protected void handleAllocate() throws EngineStateException, EngineException, AudioException, SecurityException {
        if (getSynthesizerProperties().getVoice() == null) {
            Voice voice;
            GyutanSynthesizerMode mode = (GyutanSynthesizerMode) getEngineMode();
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
logger.log(Level.DEBUG, "default voice: " + (voice != null ? voice.getName() : ""));
            getSynthesizerProperties().setVoice(voice);
        }

        //
        long newState = ALLOCATED | RESUMED;
        newState |= (getQueueManager().isQueueEmpty() ? QUEUE_EMPTY : QUEUE_NOT_EMPTY);
        setEngineState(CLEAR_ALL_STATE, newState);
    }

    /** */
    private static Path toNativeVoice(Voice voice) {
        Scanner s = new Scanner(GyutanEngineListFactory.class.getResourceAsStream("/htsvoice.csv"));
        String defalutName = null;
        while (s.hasNextLine()) {
            String[] parts = s.nextLine().split(",");
            if (defalutName == null) {
                defalutName = parts[0];
            }
            if (voice.getName().equals(parts[1])) {
                return Paths.get(System.getProperty("htsvoice.dir"), parts[0]);
            }
        }
        return Paths.get(System.getProperty("htsvoice.dir"), defalutName);
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
logger.log(Level.TRACE, "vioce: " + getSynthesizerProperties().getVoice());
            Path wave = Files.createTempFile(getClass().getName(), ".wav");
            Path voice = toNativeVoice(getSynthesizerProperties().getVoice());
            Gyutan gyutan = new Gyutan(System.getProperty("sen.home"), voice.toString());
            gyutan.synthesize(text, new FileOutputStream(wave.toFile()), null);
            byte[] wav = Files.readAllBytes(wave);
            ByteArrayInputStream bais = new ByteArrayInputStream(wav);
            // you should pass bytes to BaseAudioSegment as AudioInputStream or causes crackling!
            AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
            Files.delete(wave);
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
