/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.googlegenai.jsapi2;

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
 * Factory for the Google GenAI TTS engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/06/08 umjammer initial version <br>
 */
public class GoogleGenAITTSListFactory extends BaseEnginFactory<String> implements EngineListFactory {

    private static final Logger logger = getLogger(GoogleGenAITTSListFactory.class.getName());

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<String> domainLocale, List<WrappedVoice<String>> voices) {
        return new GoogleGenAITTSSynthesizerMode("GoogleGenAI",
                "GoogleGenAITTS/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false, false, false, voices.toArray(Voice[]::new));
    }

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<String>> geAlltVoices() {
        try {
            return GoogleGenAITTSVoice.factory.getAllVoices();
        } catch (Throwable t) {
logger.log(Level.INFO, t + " env: " + System.getenv("GOOGLE_API_KEY"));
logger.log(Level.TRACE, t.getMessage() + " env: " + System.getenv("GOOGLE_API_KEY"), t);
            return Collections.emptyList();
        }
    }
}
