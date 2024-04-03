/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.coeiroink.jsapi2;

import java.util.List;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.BaseEnginFactory;
import vavi.speech.WrappedVoice;
import vavi.speech.coeiroink.CoeiroInk.Speaker;


/**
 * Factory for the CoeiroInk engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/04/03 umjammer initial version <br>
 */
public class CoeiroInkEngineListFactory extends BaseEnginFactory<Speaker> implements EngineListFactory {

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<Speaker>> geAlltVoices() {
        return CoeiroInkVoice.factory.getAllVoices();
    }

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<Speaker> domainLocale, List<WrappedVoice<Speaker>> wrappedVoices) {
        return new CoeiroInkSynthesizerMode("CoeiroInk",
                "CoeiroInk/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false,
                false,
                false,
                wrappedVoices.toArray(Voice[]::new));
    }
}
