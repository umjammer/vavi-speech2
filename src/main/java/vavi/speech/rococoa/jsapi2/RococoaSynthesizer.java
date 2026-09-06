/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.rococoa.jsapi2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
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
import org.rococoa.Foundation;
import org.rococoa.ObjCBlocks.BlockLiteral;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSError;
import vavi.speech.WrappedVoice;
import vavix.rococoa.avfoundation.AVAudioFile;
import vavix.rococoa.avfoundation.AVAudioFormat;
import vavix.rococoa.avfoundation.AVAudioPCMBuffer;
import vavix.rococoa.avfoundation.AVSpeechSynthesisVoice;
import vavix.rococoa.avfoundation.AVSpeechSynthesizer;
import vavix.rococoa.avfoundation.AVSpeechSynthesizer.AVSpeechSynthesizerBufferCallback;
import vavix.rococoa.avfoundation.AVSpeechUtterance;

import static org.rococoa.ObjCBlocks.block;


/**
 * A Cocoa compliant {@link javax.speech.synthesis.Synthesizer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public final class RococoaSynthesizer extends BaseSynthesizer {

    /** Logger for this class. */
    private static final Logger logger = System.getLogger(RococoaSynthesizer.class.getName());

    /** */
    private AVSpeechSynthesizer synthesizer;

    /** */
//    private SynthesizerDelegate delegate;

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
        if (getSynthesizerProperties().getVoice() == null) {
            Voice voice;
            RococoaSynthesizerMode mode = (RococoaSynthesizerMode) getEngineMode();
            if (mode == null) {
                throw new EngineException("not engine mode");
            } else {
                Voice[] voices = mode.getVoices();
                if (voices == null || voices.length < 1) {
                    throw new EngineException("no voice");
                } else {
                    AVSpeechSynthesisVoice defaultNativeVoice = AVSpeechSynthesisVoice.withLanguage(Locale.getDefault().toString());
//logger.log(Level.TRACE, "default voice: " + defaultNativeVoice.getName());
                    Optional<Voice> result = Arrays.stream(voices).filter(v -> v.getName().equals(defaultNativeVoice.name())).findFirst();
                    voice = result.orElseGet(() -> voices[0]);
                }
            }
logger.log(Level.DEBUG, "default voice: " + voice.getName());
            getSynthesizerProperties().setVoice(voice);
        }

        synthesizer = AVSpeechSynthesizer.newInstance();
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
        synthesizer.release();
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
        AudioManager manager = getAudioManager();
        String locator = manager.getMediaLocator();
        InputStream in = synthesize(item);
        AudioSegment segment;
        if (locator == null) {
            segment = new BaseAudioSegment(item, in);
        } else {
            segment = new BaseAudioSegment(locator, item, in);
        }
        return segment;
    }

    /** */
    private AudioInputStream synthesize(String text) {
        try {
//logger.log(Level.TRACE, "voice: " + getSynthesizerProperties().getVoice());
            Path path = Files.createTempFile(getClass().getName(), ".wav");
            BlockLiteral bufferCallback = null;
            try {
                AVSpeechUtterance utterance = AVSpeechUtterance.of(text);
                var voice = ((WrappedVoice<AVSpeechSynthesisVoice>) getSynthesizerProperties().getVoice()).getNativeVoice();
                utterance.setVoice(voice);
                utterance.setVolume(getSynthesizerProperties().getVolume() / 100f);

                CountDownLatch cdl = new CountDownLatch(1);
                AtomicReference<AVAudioFile> audioFile = new AtomicReference<>();

                bufferCallback = block((AVSpeechSynthesizerBufferCallback) (block, audioBufferId) -> {
                    try {
                        AVAudioPCMBuffer audioBuffer = Rococoa.wrap(audioBufferId, AVAudioPCMBuffer.class);
                        if (audioBuffer == null) {
                            cdl.countDown();
                            throw new IllegalStateException("buffer is not pcm");
                        }
                        if (audioBuffer.frameLength() == 0) {
                            // done
                            cdl.countDown();
                        } else {
                            if (audioFile.get() == null) {
                                AVAudioFormat format16 = AVAudioFormat.init(3, audioBuffer.format().sampleRate(), 1, true);
                                audioFile.set(AVAudioFile.init(path.toUri(), format16.settings(), audioBuffer.format().commonFormat(), audioBuffer.format().isInterleaved()));
                                if (audioFile.get() == null) {
                                    cdl.countDown();
                                    throw new IllegalStateException("file creation failed");
                                }
                            }
                            ObjCObjectByReference outError = new ObjCObjectByReference();
                            audioFile.get().writeFromBuffer_error(audioBuffer, outError);
                            NSError error = outError.getValueAs(NSError.class);
                            if (error != null) {
                                cdl.countDown();
                                throw new IllegalStateException(error.description());
                            }
                        }
                    } catch (IOException e) {
                        cdl.countDown();
                        throw new UncheckedIOException(e);
                    }
                });

                synthesizer.writeUtterance_toBufferCallback(utterance, bufferCallback);
                cdl.await();

                if (audioFile.get() != null) {
                    audioFile.get().close();
                }

                return AudioSystem.getAudioInputStream(new ByteArrayInputStream(Files.readAllBytes(path)));
            } finally {
                Files.deleteIfExists(path);
                if (bufferCallback != null)
                    Foundation.getRococoaLibrary().releaseObjCBlock(bufferCallback.getPointer());
            }
        } catch (Exception e) {
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
