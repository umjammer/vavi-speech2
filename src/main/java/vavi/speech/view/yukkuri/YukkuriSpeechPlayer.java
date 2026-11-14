/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * Synchronizes {@link YukkuriCharacter} lip-sync and facial expression timelines
 * to speech PCM playback.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class YukkuriSpeechPlayer {

    private final YukkuriCharacter character;
    private final Runnable onFrameUpdated;
    private final ExpressionAnalyzer expressionAnalyzer;
    private final LipSyncAnalyzer lipSyncAnalyzer;

    public YukkuriSpeechPlayer(YukkuriFrame frame) {
        this(frame, new AcpExpressionAnalyzer());
    }

    public YukkuriSpeechPlayer(YukkuriFrame frame, ExpressionAnalyzer expressionAnalyzer) {
        this(frame.getCharacter(), frame::refresh, expressionAnalyzer, new LipSyncAnalyzer());
    }

    public YukkuriSpeechPlayer(
            YukkuriCharacter character,
            Runnable onFrameUpdated,
            ExpressionAnalyzer expressionAnalyzer,
            LipSyncAnalyzer lipSyncAnalyzer) {
        this.character = Objects.requireNonNull(character, "character");
        this.onFrameUpdated = onFrameUpdated != null ? onFrameUpdated : () -> {};
        this.expressionAnalyzer = Objects.requireNonNull(expressionAnalyzer, "expressionAnalyzer");
        this.lipSyncAnalyzer = Objects.requireNonNull(lipSyncAnalyzer, "lipSyncAnalyzer");
    }

    /**
     * Speaks a WAV file with synchronized lip-sync and facial expression animation.
     */
    public void play(String voiceText, Path wavFile)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile.toFile())) {
            AudioFormat baseFormat = ais.getFormat();
            AudioFormat pcmFormat = baseFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
                    ? baseFormat
                    : new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false);
            try (AudioInputStream pcmStream = pcmFormat.equals(baseFormat)
                    ? ais
                    : AudioSystem.getAudioInputStream(pcmFormat, ais)) {
                byte[] pcm = pcmStream.readAllBytes();
                play(voiceText, pcm, pcmFormat);
            }
        }
    }

    /**
     * Plays a PCM buffer with synchronized lip-sync and facial expression animation.
     */
    public void play(String voiceText, byte[] pcmData, AudioFormat format)
            throws IOException, LineUnavailableException {
        Objects.requireNonNull(pcmData, "pcmData");
        Objects.requireNonNull(format, "format");

        long durationMs = computeDurationMs(pcmData, format);
        ExpressionTimeline expressions = expressionAnalyzer.analyze(voiceText, durationMs);
        play(pcmData, format, expressions);
    }

    /**
     * Plays a PCM buffer with synchronized lip-sync and a precomputed {@link ExpressionTimeline}.
     */
    public void play(byte[] pcmData, AudioFormat format, ExpressionTimeline expressions)
            throws IOException, LineUnavailableException {
        Objects.requireNonNull(pcmData, "pcmData");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(expressions, "expressions");

        int frameSize = Math.max(1, format.getFrameSize());
        LipSyncTimeline lipSync = lipSyncAnalyzer.analyze(pcmData, format);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(format);
            line.start();

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "YukkuriLipSync");
                t.setDaemon(true);
                return t;
            });

            ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
                long posMs = line.getMicrosecondPosition() / 1000L;
                applyStateAt(posMs, lipSync, expressions);
            }, 0, 20, TimeUnit.MILLISECONDS);

            try {
                int chunkSize = Math.max(frameSize, (int) (format.getSampleRate() * frameSize * 0.05));
                int offset = 0;
                while (offset < pcmData.length) {
                    int len = Math.min(chunkSize, pcmData.length - offset);
                    len -= len % frameSize;
                    if (len <= 0) {
                        break;
                    }
                    line.write(pcmData, offset, len);
                    offset += len;
                }
                line.drain();
            } finally {
                task.cancel(false);
                scheduler.shutdownNow();
                line.stop();
                if (character.setState(character.getDefaultExpression(), LipSyncAnalyzer.Vowel.SILENT, YukkuriCharacter.BlinkPhase.OPEN)) {
                    onFrameUpdated.run();
                }
            }
        }
    }

    void applyStateAt(long posMs, LipSyncTimeline lipSync, ExpressionTimeline expressions) {
        LipSyncAnalyzer.LipSyncFrame lip = lipSync.at(posMs);
        FacialExpression expr = expressions.at(posMs, character.getDefaultExpression());
        if (character.setState(expr, lip.vowel(), computeBlinkPhase(posMs))) {
            onFrameUpdated.run();
        }
    }

    static long computeDurationMs(byte[] pcmData, AudioFormat format) {
        int frameSize = Math.max(1, format.getFrameSize());
        return Math.round((pcmData.length / (double) frameSize) * 1000.0 / format.getSampleRate());
    }

    static YukkuriCharacter.BlinkPhase computeBlinkPhase(long posMs) {
        long cycle = posMs % 3500L;
        if (cycle >= 3320L && cycle < 3370L) {
            return YukkuriCharacter.BlinkPhase.HALF;
        }
        if (cycle >= 3370L && cycle < 3450L) {
            return YukkuriCharacter.BlinkPhase.CLOSED;
        }
        if (cycle >= 3450L && cycle < 3500L) {
            return YukkuriCharacter.BlinkPhase.HALF;
        }
        return YukkuriCharacter.BlinkPhase.OPEN;
    }
}
