/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


/**
 * Time-ordered sequence of {@link FacialExpression} segments synchronized with speech playback.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class ExpressionTimeline {

    /**
     * A single timed facial expression segment.
     */
    public record Entry(long startMs, long endMs, FacialExpression expression, String segment) {

        public Entry {
            Objects.requireNonNull(expression, "expression");
            if (startMs < 0 || endMs < startMs) {
                throw new IllegalArgumentException("Invalid time range: " + startMs + ".." + endMs);
            }
        }

        public boolean contains(long positionMs) {
            return positionMs >= startMs && positionMs < endMs;
        }
    }

    private final List<Entry> entries;

    public ExpressionTimeline(List<Entry> entries) {
        List<Entry> sorted = new ArrayList<>(Objects.requireNonNull(entries, "entries"));
        sorted.sort(Comparator.comparingLong(Entry::startMs));
        this.entries = Collections.unmodifiableList(sorted);
    }

    public static ExpressionTimeline empty() {
        return new ExpressionTimeline(List.of());
    }

    public List<Entry> entries() {
        return entries;
    }

    /**
     * Returns the {@link FacialExpression} active at {@code positionMs},
     * or {@link FacialExpression#NEUTRAL} if no entry covers that timestamp.
     */
    public FacialExpression at(long positionMs) {
        return at(positionMs, FacialExpression.NEUTRAL);
    }

    /**
     * Returns the {@link FacialExpression} active at {@code positionMs},
     * or {@code defaultExpression} if no entry covers that timestamp.
     */
    public FacialExpression at(long positionMs, FacialExpression defaultExpression) {
        Objects.requireNonNull(defaultExpression, "defaultExpression");
        for (Entry entry : entries) {
            if (entry.contains(positionMs)) {
                return entry.expression();
            }
        }
        if (!entries.isEmpty() && positionMs >= entries.getLast().endMs()) {
            return entries.getLast().expression();
        }
        return defaultExpression;
    }
}
