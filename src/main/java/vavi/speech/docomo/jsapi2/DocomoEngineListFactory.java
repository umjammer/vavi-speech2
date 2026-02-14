/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.docomo.jsapi2;

import java.util.List;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.BaseEnginFactory;
import vavi.speech.WrappedVoice;


/**
 * Factory for the DocomoAIAgentAPI engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2024/03/21 umjammer initial version <br>
 */
public class DocomoEngineListFactory extends BaseEnginFactory<Voice> implements EngineListFactory {

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<Voice>> geAlltVoices() {
        return DocomoVoice.factory.getAllVoices();
    }

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<Voice> domainLocale, List<WrappedVoice<Voice>> wrappedVoices) {
        return new DocomoSynthesizerMode("Docomo",
                "Docomo/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false,
                false,
                false,
                wrappedVoices.toArray(Voice[]::new));
    }
}
