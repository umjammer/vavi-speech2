/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.voicestudio.jsapi2;

import java.util.List;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.BaseEnginFactory;
import vavi.speech.WrappedVoice;
import vavi.speech.voicestudio.VoiceStudio;


/**
 * Factory for the VoiceStudio engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2026/10/11 umjammer initial version <br>
 */
public class VoiceStudioEngineListFactory extends BaseEnginFactory<VoiceStudio.Voice> implements EngineListFactory {

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<VoiceStudio.Voice>> geAlltVoices() {
        return VoiceStudioVoice.factory.getAllVoices();
    }

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<VoiceStudio.Voice> domainLocale, List<WrappedVoice<VoiceStudio.Voice>> wrappedVoices) {
        return new VoiceStudioSynthesizerMode("VoiceStudio",
                "VoiceStudio/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false,
                false,
                false,
                wrappedVoices.toArray(Voice[]::new));
    }
}
