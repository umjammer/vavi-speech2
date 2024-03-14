/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.aquestalk10.jsapi2;

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
import vavi.speech.aquestalk10.jna.AquesTalk10.AQTK_VOICE;

import static java.lang.System.getLogger;


/**
 * Factory for the AquesTalk10 Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public class AquesTalk10EngineListFactory extends BaseEnginFactory<AQTK_VOICE> implements EngineListFactory {

    private static final Logger logger = getLogger(AquesTalk10EngineListFactory.class.getName());

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<AQTK_VOICE>> geAlltVoices() {
try {
        return AquesTalk10Voice.factory.getAllVoices();
} catch (Throwable e) {
 logger.log(Level.WARNING, "AquesTalk10 doesn't work on arm64", e);
        return Collections.emptyList();
}
    }

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<AQTK_VOICE> domainLocale, List<WrappedVoice<AQTK_VOICE>> wrappedVoices) {
        return new AquesTalk10SynthesizerMode("AquesTalk10",
                "AquesTalk10/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false,
                false,
                true,
                wrappedVoices.toArray(Voice[]::new));
    }
}
