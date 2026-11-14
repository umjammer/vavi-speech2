/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.io.IOException;


/**
 * Transforms voice text into a timed {@link ExpressionTimeline}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public interface ExpressionAnalyzer {

    /**
     * Analyzes {@code text} and produces an {@link ExpressionTimeline} spanning {@code durationMs}.
     *
     * @param text       spoken text
     * @param durationMs total speech duration in milliseconds
     * @return facial expression timeline
     * @throws IOException if an underlying agent or analyzer fails
     */
    ExpressionTimeline analyze(String text, long durationMs) throws IOException;
}
