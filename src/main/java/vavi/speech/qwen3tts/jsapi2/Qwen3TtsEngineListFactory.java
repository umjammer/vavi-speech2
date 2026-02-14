/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.qwen3tts.jsapi2;

import java.util.List;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.BaseEnginFactory;
import vavi.speech.WrappedVoice;
import vavi.speech.qwen3tts.Qwen3Tts;


/**
 * Factory for the Qwen3-TTS engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2026/02/13 umjammer initial version <br>
 */
public class Qwen3TtsEngineListFactory extends BaseEnginFactory<Qwen3Tts.Voice> implements EngineListFactory {

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<Qwen3Tts.Voice>> geAlltVoices() {
        return Qwen3TtsVoice.factory.getAllVoices();
    }

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<Qwen3Tts.Voice> domainLocale, List<WrappedVoice<Qwen3Tts.Voice>> wrappedVoices) {
        return new Qwen3TtsSynthesizerMode("Qwen3TTS",
                "Qwen3TTS/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false,
                false,
                false,
                wrappedVoices.toArray(Voice[]::new));
    }
}
