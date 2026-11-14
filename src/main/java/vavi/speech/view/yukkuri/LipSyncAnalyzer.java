/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;


/**
 * LPC formant-based vowel recognizer for lip-sync animation,
 * inspired by hecomi's MMD4Mecanim-LipSync-Plugin ({@code LipSyncCore.cs}).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class LipSyncAnalyzer {

    public enum Vowel {
        SILENT, A, I, U, E, O
    }

    public record LipSyncFrame(long timeMs, Vowel vowel, float volume, float f1Hz, float f2Hz) {
    }

    // Standard Japanese vowel formant centers (F1 Hz, F2 Hz)
    private static final float[][] VOWEL_FORMANTS = {
            {750f, 1250f}, // A
            {300f, 2200f}, // I
            {350f, 1100f}, // U
            {500f, 1900f}, // E
            {500f, 900f},  // O
    };

    private static final Vowel[] VOWELS = {Vowel.A, Vowel.I, Vowel.U, Vowel.E, Vowel.O};

    private final float volumeThreshold;
    private final long stepMs;
    private final int windowSize;

    public LipSyncAnalyzer() {
        this(0.01f, 20L, 1024);
    }

    public LipSyncAnalyzer(float volumeThreshold, long stepMs, int windowSize) {
        this.volumeThreshold = volumeThreshold;
        this.stepMs = Math.max(1L, stepMs);
        this.windowSize = Math.max(128, windowSize);
    }

    /**
     * Analyzes a PCM buffer into a {@link LipSyncTimeline}.
     */
    public LipSyncTimeline analyze(byte[] pcmData, AudioFormat format) {
        if (pcmData == null || pcmData.length == 0 || format == null) {
            return LipSyncTimeline.empty();
        }
        float[] mono = toMonoFloat(pcmData, format);
        float sampleRate = format.getSampleRate();
        if (mono.length == 0 || sampleRate <= 0) {
            return LipSyncTimeline.empty();
        }

        long durationMs = Math.round(mono.length * 1000.0 / sampleRate);
        int hopSamples = Math.max(1, (int) Math.round(sampleRate * stepMs / 1000.0));
        List<LipSyncFrame> frames = new ArrayList<>();
        float[] window = new float[windowSize];

        for (int start = 0; start < mono.length; start += hopSamples) {
            int len = Math.min(windowSize, mono.length - start);
            System.arraycopy(mono, start, window, 0, len);
            if (len < windowSize) {
                java.util.Arrays.fill(window, len, windowSize, 0f);
            }
            long timeMs = Math.round(start * 1000.0 / sampleRate);
            LipSyncFrame frame = analyzeWindow(timeMs, window, sampleRate);
            frames.add(frame);
        }

        return new LipSyncTimeline(frames, stepMs, durationMs);
    }

    /**
     * Analyzes a single audio window using Hamming windowing, pre-emphasis,
     * Levinson-Durbin LPC, and F1/F2 formant peak detection.
     */
    public LipSyncFrame analyzeWindow(long timeMs, float[] samples, float sampleRate) {
        float rms = computeRms(samples);
        if (rms < volumeThreshold) {
            return new LipSyncFrame(timeMs, Vowel.SILENT, rms, 0f, 0f);
        }

        int n = samples.length;
        double[] windowed = new double[n];
        double prev = 0.0;
        for (int i = 0; i < n; i++) {
            double preEmphasized = samples[i] - 0.97 * prev;
            prev = samples[i];
            double hamming = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1));
            windowed[i] = preEmphasized * hamming;
        }

        int order = Math.clamp(Math.round(sampleRate / 1000f) + 2, 10, 32);
        double[] lpc = levinsonDurbin(windowed, order);
        float[] formants = extractFormants(lpc, sampleRate);
        float f1 = formants[0];
        float f2 = formants[1];

        Vowel vowel = classifyVowel(f1, f2);
        return new LipSyncFrame(timeMs, vowel, Math.min(1.0f, rms * 5.0f), f1, f2);
    }

    private static float computeRms(float[] samples) {
        double sum = 0.0;
        for (float s : samples) {
            sum += s * s;
        }
        return (float) Math.sqrt(sum / samples.length);
    }

    private static double[] levinsonDurbin(double[] x, int order) {
        double[] r = new double[order + 1];
        for (int k = 0; k <= order; k++) {
            double sum = 0.0;
            for (int i = 0; i < x.length - k; i++) {
                sum += x[i] * x[i + k];
            }
            r[k] = sum;
        }

        double[] a = new double[order + 1];
        double[] aPrev = new double[order + 1];
        a[0] = 1.0;

        if (r[0] <= 1e-12) {
            return a;
        }

        double error = r[0];
        for (int k = 1; k <= order; k++) {
            double acc = 0.0;
            for (int j = 1; j < k; j++) {
                acc += a[j] * r[k - j];
            }
            double lambda = -(r[k] + acc) / error;

            System.arraycopy(a, 0, aPrev, 0, k);
            for (int j = 1; j < k; j++) {
                a[j] = aPrev[j] + lambda * aPrev[k - j];
            }
            a[k] = lambda;
            error *= (1.0 - lambda * lambda);
            if (error <= 1e-12) {
                break;
            }
        }
        return a;
    }

    private static float[] extractFormants(double[] lpc, float sampleRate) {
        int minHz = 100;
        int maxHz = Math.min(3500, (int) (sampleRate / 2f) - 40);
        int stepHz = 20;
        int bins = Math.max(3, (maxHz - minHz) / stepHz + 1);
        double[] spectrum = new double[bins];

        for (int i = 0; i < bins; i++) {
            double freq = minHz + i * stepHz;
            double omega = 2.0 * Math.PI * freq / sampleRate;
            double re = 1.0;
            double im = 0.0;
            for (int k = 1; k < lpc.length; k++) {
                re += lpc[k] * Math.cos(-omega * k);
                im += lpc[k] * Math.sin(-omega * k);
            }
            double magSq = re * re + im * im;
            spectrum[i] = 1.0 / Math.max(1e-12, Math.sqrt(magSq));
        }

        float f1 = 0f;
        float f2 = 0f;
        for (int i = 1; i < bins - 1; i++) {
            if (spectrum[i] > spectrum[i - 1] && spectrum[i] > spectrum[i + 1]) {
                float freq = minHz + i * stepHz;
                if (f1 == 0f && freq >= 200f) {
                    f1 = freq;
                } else if (f1 > 0f && freq >= f1 + 150f) {
                    f2 = freq;
                    break;
                }
            }
        }
        return new float[] {f1, f2};
    }

    static Vowel classifyVowel(float f1, float f2) {
        if (f1 <= 0f || f2 <= 0f) {
            return Vowel.A;
        }
        Vowel best = Vowel.A;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < VOWELS.length; i++) {
            double df1 = (f1 - VOWEL_FORMANTS[i][0]) / 250.0;
            double df2 = (f2 - VOWEL_FORMANTS[i][1]) / 600.0;
            double dist = df1 * df1 + df2 * df2;
            if (dist < bestDist) {
                bestDist = dist;
                best = VOWELS[i];
            }
        }
        return best;
    }

    static float[] toMonoFloat(byte[] pcmData, AudioFormat format) {
        int channels = Math.max(1, format.getChannels());
        int sampleSizeBytes = Math.max(1, format.getSampleSizeInBits() / 8);
        int frameSize = channels * sampleSizeBytes;
        int frameCount = pcmData.length / frameSize;
        float[] mono = new float[frameCount];
        boolean bigEndian = format.isBigEndian();

        for (int i = 0; i < frameCount; i++) {
            double sum = 0.0;
            for (int ch = 0; ch < channels; ch++) {
                int offset = i * frameSize + ch * sampleSizeBytes;
                double sample;
                if (sampleSizeBytes == 1) {
                    sample = ((pcmData[offset] & 0xFF) - 128) / 128.0;
                } else {
                    int lo = pcmData[offset] & 0xFF;
                    int hi = pcmData[offset + 1] & 0xFF;
                    short s = bigEndian ? (short) ((lo << 8) | hi) : (short) ((hi << 8) | lo);
                    sample = s / 32768.0;
                }
                sum += sample;
            }
            mono[i] = (float) (sum / channels);
        }
        return mono;
    }
}
