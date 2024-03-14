/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.openjtalk.jsapi2;

import java.util.List;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.spi.EngineListFactory;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.BaseEnginFactory;
import vavi.speech.WrappedVoice;
import vavi.speech.openjtalk.OpenJTalkWrapper;
import vavi.speech.openjtalk.OpenJTalkWrapper.VoiceFileInfo;


/**
 * Factory for the OpenJTalk Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/26 umjammer initial version <br>
 */
public class OpenJTalkEngineListFactory extends BaseEnginFactory<OpenJTalkWrapper.VoiceFileInfo> implements EngineListFactory {

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<VoiceFileInfo>> geAlltVoices() {
        return OpenJTalkVoice.factory.getAllVoices();
    }

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<VoiceFileInfo> domainLocale, List<WrappedVoice<VoiceFileInfo>> wrappedVoices) {
        return new OpenJTalkSynthesizerMode("OpenJTalk",
                "OpenJTalk/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false,
                false,
                false,
                wrappedVoices.toArray(Voice[]::new));
    }
}
