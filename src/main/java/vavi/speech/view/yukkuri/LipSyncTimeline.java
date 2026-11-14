/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Time-indexed lip-sync frames produced by {@link LipSyncAnalyzer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class LipSyncTimeline {

    private final List<LipSyncAnalyzer.LipSyncFrame> frames;
    private final long stepMs;
    private final long durationMs;

    public LipSyncTimeline(List<LipSyncAnalyzer.LipSyncFrame> frames, long stepMs, long durationMs) {
        this.frames = Collections.unmodifiableList(Objects.requireNonNull(frames, "frames"));
        this.stepMs = Math.max(1, stepMs);
        this.durationMs = Math.max(0, durationMs);
    }

    public static LipSyncTimeline empty() {
        return new LipSyncTimeline(List.of(), 20, 0);
    }

    public List<LipSyncAnalyzer.LipSyncFrame> frames() {
        return frames;
    }

    public long stepMs() {
        return stepMs;
    }

    public long durationMs() {
        return durationMs;
    }

    /**
     * Returns the {@link LipSyncAnalyzer.LipSyncFrame} at {@code positionMs},
     * or a silent frame if out of range.
     */
    public LipSyncAnalyzer.LipSyncFrame at(long positionMs) {
        if (frames.isEmpty() || positionMs < 0 || positionMs >= durationMs) {
            return new LipSyncAnalyzer.LipSyncFrame(Math.max(0, positionMs), LipSyncAnalyzer.Vowel.SILENT, 0f, 0f, 0f);
        }
        int index = (int) Math.min(frames.size() - 1, positionMs / stepMs);
        return frames.get(index);
    }
}
