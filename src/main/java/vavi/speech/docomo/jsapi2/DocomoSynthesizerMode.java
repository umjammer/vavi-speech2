/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.docomo.jsapi2;

import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;


/**
 * Synthesizer mode for DocomoAIAgentAPI.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2024/03/21 umjammer initial version <br>
 * @deprecated end of service
 */
@Deprecated
public final class DocomoSynthesizerMode extends SynthesizerMode implements EngineFactory {

    /**
     * Constructs a new object.
     */
    public DocomoSynthesizerMode() {
        super("Docomo", null,
                null, null, null, null);
    }

    /**
     * Constructs a new object.
     *
     * @param locale the locale associated with this mode
     */
    public DocomoSynthesizerMode(SpeechLocale locale) {
        super("Docomo", null, null, null, null,
                new Voice[] {new Voice(locale, null, Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, Voice.VARIANT_DONT_CARE)});
    }

    /**
     * Constructs a new object.
     *
     * Docomo synthesizer does not support ssml
     *
     * @param engineName the name of the engine
     * @param modeName the name of the mode
     */
    public DocomoSynthesizerMode(String engineName,
                                 String modeName,
                                 Boolean running,
                                 Boolean supportsLetterToSound,
                                 Boolean supportsMarkup,
                                 Voice[] voices) {
        super(engineName, modeName, running, supportsLetterToSound, supportsMarkup, voices);
    }

    @Override
    public Engine createEngine() throws IllegalArgumentException, EngineException {
        return new DocomoSynthesizer(this);
    }
}
