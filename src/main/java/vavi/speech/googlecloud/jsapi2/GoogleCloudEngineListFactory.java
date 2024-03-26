/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlecloud.jsapi2;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.List;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.BaseEnginFactory;
import vavi.speech.WrappedVoice;

import static java.lang.System.getLogger;


/**
 * Factory for the Google Cloud engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public class GoogleCloudEngineListFactory extends BaseEnginFactory<com.google.cloud.texttospeech.v1.Voice> implements EngineListFactory {

    private static final Logger logger = getLogger(GoogleCloudEngineListFactory.class.getName());

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<com.google.cloud.texttospeech.v1.Voice> domainLocale, List<WrappedVoice<com.google.cloud.texttospeech.v1.Voice>> voices) {
        return new GoogleCloudTextToSpeechSynthesizerMode("GoogleCloud",
                "GoogleCloudTextToSpeech/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false, false, false, voices.toArray(Voice[]::new));
    }

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<com.google.cloud.texttospeech.v1.Voice>> geAlltVoices() {
        try {
            return GoogleCloudTextToSpeechVoice.factory.getAllVoices();
        } catch (Throwable t) {
logger.log(Level.WARNING, t.getMessage() + " env: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"), t);
            return Collections.emptyList();
        }
    }
}
