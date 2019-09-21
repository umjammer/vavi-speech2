/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.rococoa.jsapi2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.speech.AudioException;
import javax.speech.EngineException;
import javax.speech.EngineStateException;
import javax.speech.recognition.Grammar;
import javax.speech.recognition.GrammarException;
import javax.speech.recognition.GrammarManager;
import javax.speech.recognition.Result;
import javax.speech.recognition.ResultEvent;
import javax.speech.recognition.RuleGrammar;

import org.jvoicexml.jsapi2.BaseEngineProperties;
import org.jvoicexml.jsapi2.recognition.BaseRecognizer;
import org.jvoicexml.jsapi2.recognition.BaseResult;
import org.jvoicexml.jsapi2.recognition.GrammarDefinition;


/**
 * A Rococoa recognizer.
 *
 * @author Dirk Schnelle-Walka
 */
public final class RococoaRecognizer extends BaseRecognizer {
    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(RococoaRecognizer.class
            .getName());

    /** SAPI recognizer Handle. **/
    private long recognizerHandle;

    /**
     * Constructs a new object.
     * 
     * @param mode
     *            the recognizer mode.
     */
    public RococoaRecognizer(final RococoaRecognizerMode mode) {
        super(mode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Grammar> getBuiltInGrammars() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAllocate() throws EngineStateException, EngineException,
            AudioException, SecurityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleDeallocate() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void handlePause() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void handlePause(int flags) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handleResume(InputStream in) throws EngineStateException {
        final GrammarManager manager = getGrammarManager();
        final Grammar[] grammars = manager.listGrammars();
        final String[] grammarSources = new String[grammars.length];
        int i = 0;
        for (Grammar grammar : grammars) {
            try {
                final File file = File.createTempFile("sapi", "xml");
                file.deleteOnExit();
                final FileOutputStream out = new FileOutputStream(file);

                StringBuffer xml = new StringBuffer();
                xml.append(grammar.toString());
                int index = xml.indexOf("06/grammar");
                xml.insert(index + 11, " xml:lang=\"de-DE\" ");
                out.write(xml.toString().getBytes());
                out.close();
                grammarSources[i] = file.getCanonicalPath();
                // System.out.println(xml);
                // System.out.println(grammarSources[i]);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ++i;
        }
        return macResume(recognizerHandle, grammarSources);
    }

    private native boolean macResume(long handle, String[] grammars)
            throws EngineStateException;

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean setGrammars(
            Collection<GrammarDefinition> grammarDefinition) {
        return false;
    }

    public boolean setGrammar(final String grammarPath) {
        throw new UnsupportedOperationException();
    }

    void startRecognition() {
        throw new UnsupportedOperationException();
    }

    /**
     * Notification from the SAPI recognizer about a recognition result.
     * 
     * @param utterance
     *            the detected utterance
     */
    private void reportResult(final String utterance) {

        System.out.println("Java Code " + utterance);

        final RuleGrammar grammar = currentGrammar; // current grammar is not
                                                    // available
        System.out.println(grammar);

        final BaseResult result;
        try {
            result = new BaseResult(grammar, utterance);
        } catch (GrammarException e) {
            LOGGER.warning(e.getMessage());
            return;
        }

        final ResultEvent created = new ResultEvent(result,
                ResultEvent.RESULT_CREATED, false, false);
        postResultEvent(created);

        final ResultEvent grammarFinalized = new ResultEvent(result,
                ResultEvent.GRAMMAR_FINALIZED);
        postResultEvent(grammarFinalized);

        if (result.getResultState() == Result.REJECTED) {
            final ResultEvent rejected = new ResultEvent(result,
                    ResultEvent.RESULT_REJECTED, false, false);
            postResultEvent(rejected);
        } else {
            final ResultEvent accepted = new ResultEvent(result,
                    ResultEvent.RESULT_ACCEPTED, false, false);
            postResultEvent(accepted);
        }
    }

    @Override
    protected void handleReleaseFocus() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void handleRequestFocus() {
        // TODO Auto-generated method stub

    }

    /* */
    @Override
    protected AudioFormat getAudioFormat() {
        return new AudioFormat(16000, 2, 1, true, false);
    }

    /* */
    @Override
    protected void handlePropertyChangeRequest(
            final BaseEngineProperties properties,
            final String propName, final Object oldValue,
            final Object newValue) {
        LOGGER.warning("changing property '" + propName
                + "' to '" + newValue + "' ignored");
    }
}
