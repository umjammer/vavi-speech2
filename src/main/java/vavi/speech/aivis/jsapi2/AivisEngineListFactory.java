/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.aivis.jsapi2;

import java.util.List;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.BaseEnginFactory;
import vavi.speech.WrappedVoice;
import vavi.speech.aivis.Aivis.AivisSpeaker;


/**
 * Factory for the Aivis engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2024/12/14 umjammer initial version <br>
 */
public class AivisEngineListFactory extends BaseEnginFactory<AivisSpeaker> implements EngineListFactory {

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<AivisSpeaker>> geAlltVoices() {
        return AivisVoice.factory.getAllVoices();
    }

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<AivisSpeaker> domainLocale, List<WrappedVoice<AivisSpeaker>> wrappedVoices) {
        return new AivisSynthesizerMode("Aivis",
                "Aivis/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false,
                false,
                false,
                wrappedVoices.toArray(Voice[]::new));
    }
}
