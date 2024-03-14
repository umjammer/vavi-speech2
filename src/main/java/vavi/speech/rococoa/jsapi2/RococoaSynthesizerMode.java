/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.rococoa.jsapi2;

import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;


/**
 * Synthesizer mode for Cocoa.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public final class RococoaSynthesizerMode extends SynthesizerMode implements EngineFactory {

    /**
     * Constructs a new object.
     */
    public RococoaSynthesizerMode() {
        super("Rococoa", null,
                null, null, null, null);
    }

    /**
     * Constructs a new object.
     *
     * @param locale the locale associated with this mode
     */
    public RococoaSynthesizerMode(SpeechLocale locale) {
        super("Rococoa", null, null, null, null,
                new Voice[] {new Voice(locale, null, Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE)});
    }

    /**
     * Constructs a new object.
     *
     * Mac synthesizer does not support ssml
     *
     * @param engineName the name of the engine
     * @param modeName the name of the mode
     */
    public RococoaSynthesizerMode(String engineName,
                                  String modeName,
                                  Boolean running,
                                  Boolean supportsLetterToSound,
                                  Boolean supportsMarkup,
                                  Voice[] voices) {
        super(engineName, modeName, running, supportsLetterToSound, false, voices);
    }

    @Override
    public Engine createEngine() throws IllegalArgumentException, EngineException {
        return new RococoaSynthesizer(this);
    }
}
