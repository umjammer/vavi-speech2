/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.aquestalk10.jsapi2;

import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.SpeechLocale;
import javax.speech.spi.EngineFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;


/**
 * Synthesizer mode for AquesTalk10.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public final class AquesTalk10SynthesizerMode extends SynthesizerMode implements EngineFactory {

    /**
     * Constructs a new object.
     */
    public AquesTalk10SynthesizerMode() {
        super();
    }

    /**
     * Constructs a new object.
     * 
     * @param locale the locale associated with this mode
     */
    public AquesTalk10SynthesizerMode(SpeechLocale locale) {
        super(locale);
    }

    /**
     * Constructs a new object.
     *
     * Mac synthesizer does not support ssml
     *
     * @param engineName the name of the engine
     * @param modeName the name of the mode
     */
    public AquesTalk10SynthesizerMode(String engineName,
                                      String modeName,
                                      Boolean running,
                                      Boolean supportsLetterToSound,
                                      Boolean supportsMarkup,
                                      Voice[] voices) {
        super(engineName, modeName, running, supportsLetterToSound, false, voices);
    }

    @Override
    public Engine createEngine() throws IllegalArgumentException, EngineException {
        return new AquesTalk10Synthesizer(this);
    }
}
