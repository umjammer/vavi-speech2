/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech;

import java.util.List;
import java.util.Locale;
import javax.speech.SpeechLocale;
import javax.speech.synthesis.Voice;


/**
 * WrappedVoice.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-14 nsano initial version <br>
 */
public abstract class WrappedVoice<V> extends Voice {

    /** */
    protected V nativeVoice;

    /** */
    protected WrappedVoice(V nativeVoice) {
        this.nativeVoice = nativeVoice;
    }

    /** */
    protected WrappedVoice(SpeechLocale locale, String name, int gender, int age, int variant, V nativeVoice) {
        super(locale, name, gender, age, variant);
        this.nativeVoice = nativeVoice;
    }

    /** */
    public V getNativeVoice() {
        return nativeVoice;
    }

    /** */
    public abstract List<V> getAllNativeVoices();

    /** */
    public abstract List<WrappedVoice<V>> getAllVoices();

    /** */
    public abstract String getDomain();

    /** */
    public abstract Locale getLocale();
}
