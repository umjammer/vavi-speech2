/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.speech.EngineList;
import javax.speech.EngineMode;
import javax.speech.synthesis.SynthesizerMode;

import static java.lang.System.getLogger;


/**
 * BaseEnginFactory.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-03-14 nsano initial version <br>
 */
public abstract class BaseEnginFactory<V> {

    private static final Logger logger = getLogger(BaseEnginFactory.class.getName());

    /**
     * Used to be able to generate a list of voices based on unique
     * combinations of domain/locale pairs.
     */
    protected static class DomainLocale<V> {

        /** The domain. */
        private final String domain;

        /** The locale. */
        private final Locale locale;

        /** Voices for the current domain and locale. */
        private final List<WrappedVoice<V>> voices;

        /**
         * Constructs a new object.
         *
         * @param domain the domain to use
         * @param locale the locale to use
         */
        public DomainLocale(String domain, Locale locale) {
            this.domain = domain;
            this.locale = locale;
            this.voices = new ArrayList<>();
        }

        /**
         * See if two DomainLocale objects are equal.
         * The voices are NOT compared.
         *
         * @param o, the object to compare to
         * @return true if the domain and locale are both equal, else
         * false
         */
        public boolean equals(Object o) {
            if (!(o instanceof DomainLocale)) {
                return false;
            }
            return (domain.equals(((DomainLocale<?>) o).getDomain())
                    && locale.equals(((DomainLocale<?>) o).getLocale()));
        }

        /**
         * Gets the domain.
         *
         * @return the domain
         */
        public String getDomain() {
            return domain;
        }

        /**
         * Gets the locale.
         *
         * @return the locale
         */
        public Locale getLocale() {
            return locale;
        }

        /**
         * Adds a voice to this instance.
         *
         * @param voice the voice to add
         */
        public void addVoice(WrappedVoice<V> voice) {
            voices.add(voice);
        }

        /**
         * Gets the voices of this instance.
         *
         * @return all of the voices that have been added to this
         * instance.
         */
        public List<WrappedVoice<V>> getVoices() {
            return voices;
        }
    }

    /** Retrieves all voices. */
    protected abstract List<WrappedVoice<V>> geAlltVoices();

    /** */
    protected abstract SynthesizerMode createSynthesizerMode(DomainLocale<V> domainLocale, List<WrappedVoice<V>> voices);

    /** */
    protected EngineList createEngineListForSynthesizer(EngineMode require) {
        // Must be a synthesizer.
        if (require != null && !(require instanceof SynthesizerMode)) {
            return null;
        }
logger.log(Level.TRACE, getClass().getSimpleName() + " --------");

        // get all voices available
        List<WrappedVoice<V>> voices = null;
        try {
            voices = geAlltVoices();
        } catch (Throwable t) {
logger.log(Level.WARNING, t.getMessage(), t);
        }
        if (voices == null)
            voices = Collections.emptyList();
logger.log(Level.TRACE, "voices: " + voices.size());

        // We want to get all combinations of domains and locales
        List<DomainLocale<V>> domainLocaleList = new ArrayList<>();
        for (WrappedVoice<V> voice : voices) {
            DomainLocale<V> dl = new DomainLocale<>(voice.getDomain(), voice.getLocale());
            // If we find the domain locale in the set, add the existing one
            // otherwise add the template
            DomainLocale<V> dlentry = getItem(domainLocaleList, dl);
            if (dlentry == null) {
                domainLocaleList.add(dl);
                dlentry = dl;
            }
            dlentry.addVoice(voice);
        }

        // SynthesizerModes that will be created from combining domain/locale
        // with voice names
        List<SynthesizerMode> synthesizerModes = new ArrayList<>();

        // build list of SynthesizerModeDesc's for each domain/locale
        // combination
        for (DomainLocale<V> domainLocale : domainLocaleList) {

            // iterate through the voices in a different order
            voices = domainLocale.getVoices();

            SynthesizerMode mode = createSynthesizerMode(domainLocale, voices);

            if (require == null || mode.match(require)) {
                synthesizerModes.add(mode);
logger.log(Level.TRACE, "MODE: " + mode + ", voices: " + voices.size());
            }
        }

        EngineList el;
        if (synthesizerModes.isEmpty()) {
            el = null;
        } else {
logger.log(Level.DEBUG, "-------- " + getClass().getSimpleName() + " MODES: " + synthesizerModes.size() + ", voices: " + synthesizerModes.stream().mapToInt(m -> m.getVoices().length).sum());
            el = new EngineList(synthesizerModes.toArray(EngineMode[]::new));
        }
        return el;
    }

    /**
     * Gets an item out of a vector.
     *
     * @param vector the vector to search
     * @param o      the object to look for using vector.get(i).equals(o)
     * @return the item if it exists in the vector, else null
     */
    private DomainLocale<V> getItem(List<DomainLocale<V>> vector, DomainLocale<V> o) {
        int index = vector.indexOf(o);
        if (index < 0) {
            return null;
        }
        return vector.get(index);
    }
}
