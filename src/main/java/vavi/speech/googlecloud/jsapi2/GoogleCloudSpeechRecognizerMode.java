/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlecloud.jsapi2;

import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.SpeechLocale;
import javax.speech.recognition.RecognizerMode;
import javax.speech.spi.EngineFactory;


/**
 * GoogleCloudSpeechRecognizerMode.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public final class GoogleCloudSpeechRecognizerMode extends RecognizerMode implements EngineFactory {

    /**
     * Constructs a new object.
     */
    public GoogleCloudSpeechRecognizerMode() {
        super("GoogleCloudSpeech", "GoogleCloudSpeech", Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, RecognizerMode.MEDIUM_SIZE, null, null);
    }

    /**
     * Constructs a new object.
     *
     * @param locale the locale associated with this mode
     */
    public GoogleCloudSpeechRecognizerMode(final SpeechLocale locale) {
        super(locale);
    }

    @Override
    public Engine createEngine() throws IllegalArgumentException, EngineException {
        return new GoogleCloudSpeechRecognizer(this);
    }
}
