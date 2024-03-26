/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.rococoa.jsapi2;

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
import vavix.rococoa.avfoundation.AVSpeechSynthesisVoice;

import static java.lang.System.getLogger;


/**
 * Factory for the Mac Speech engine.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/09/20 umjammer initial version <br>
 */
public class RococoaEngineListFactory extends BaseEnginFactory<AVSpeechSynthesisVoice> implements EngineListFactory {

    private static final Logger logger = getLogger(RococoaEngineListFactory.class.getName());

    @Override
    public EngineList createEngineList(EngineMode require) {
        return createEngineListForSynthesizer(require);
    }

    @Override
    protected List<WrappedVoice<AVSpeechSynthesisVoice>> geAlltVoices() {
        try {
            List<WrappedVoice<AVSpeechSynthesisVoice>> voices = RococoaVoice.factory.getAllVoices();
            if (voices != null) {
                return voices;
            }
logger.log(Level.WARNING, "voices are null, something is wrong");
        } catch (Throwable t) {
logger.log(Level.WARNING, t.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    protected SynthesizerMode createSynthesizerMode(DomainLocale<AVSpeechSynthesisVoice> domainLocale, List<WrappedVoice<AVSpeechSynthesisVoice>> wrappedVoices) {
        return new RococoaSynthesizerMode("Rococoa",
                "Rococoa/" + domainLocale.getDomain() + "/" + domainLocale.getLocale(),
                false,
                false,
                false,
                wrappedVoices.toArray(Voice[]::new));
    }
}
