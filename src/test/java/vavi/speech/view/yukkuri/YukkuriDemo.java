/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sound.sampled.AudioFormat;
import javax.speech.AudioManager;
import javax.speech.Engine;
import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Voice;

import vavi.speech.voicevox.jsapi2.VoiceVoxSynthesizerMode;
import vavi.util.Debug;


/**
 * Interactive demo for {@link YukkuriFrame} + {@link YukkuriSpeechPlayer} + {@link AcpExpressionAnalyzer}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class YukkuriDemo {

    /**
     * @param args [characterDir] [voiceText]
     */
    public static void main(String[] args) throws Exception {
        Path characterDir = Path.of(args.length > 0 ? args[0] : "tmp/神威式魔理沙");
        String voiceText = args.length > 1 ? args[1] : "ゆっくりしていってね！今日はとても楽しいのだ。";

        Synthesizer synth = (Synthesizer) EngineManager.createEngine(new VoiceVoxSynthesizerMode());

        Voice voice = Arrays.stream(((SynthesizerMode) synth.getEngineMode()).getVoices()).filter(v -> v.getName().equals("ずんだもん(ノーマル)")).findFirst().get();
        synth.getSynthesizerProperties().setSpeakingRate(120); // 50 ~ 100 ~ 200 TODO not spec
        synth.getSynthesizerProperties().setPitch(16); // 1 ~ 16 ~ 31 TODO not spec
        synth.getSynthesizerProperties().setVoice(voice);
        synth.getSynthesizerProperties().setVolume(30); // 0 ~ 100

        AudioManager am = synth.getAudioManager();

        ByteArrayOutputStream pcmOut = new ByteArrayOutputStream();
        am.setMediaLocator(
                "playback://audio?encoding=pcm&rate=16000&bits=16&channels=1&signed=true&endian=little",
                pcmOut
        );

        synth.allocate();
        synth.waitEngineState(Engine.ALLOCATED);
        synth.resume();
        synth.waitEngineState(Synthesizer.RESUMED);

        YukkuriFrame frame = new YukkuriFrame(characterDir);
        frame.setLocation(200, 200);
        frame.setVisible(true);
        frame.setFloating(true);

        try (AcpExpressionAnalyzer analyzer = new AcpExpressionAnalyzer();
             ExecutorService acpExecutor = Executors.newSingleThreadExecutor(r -> {
                 Thread t = new Thread(r, "YukkuriAcpPrefetch");
                 t.setDaemon(true);
                 return t;
             })) {
            frame.getCharacter().setDefaultExpression(analyzer.analyzeDefaultExpression(voiceText));
            frame.refresh();

            YukkuriSpeechPlayer player = new YukkuriSpeechPlayer(frame, analyzer);
            AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);

            List<String> segments = Arrays.stream(voiceText.split("。"))
                    .filter(s -> !s.isBlank())
                    .toList();

            if (!segments.isEmpty()) {
                byte[] currentPcm = synthesize(synth, pcmOut, segments.get(0));
                long currentDurationMs = YukkuriSpeechPlayer.computeDurationMs(currentPcm, format);
                String firstSegment = segments.get(0);
                Future<ExpressionTimeline> currentExprFuture = acpExecutor.submit(
                        () -> analyzer.analyze(firstSegment, currentDurationMs));

                for (int i = 0; i < segments.size(); i++) {
                    byte[] nextPcm = null;
                    Future<ExpressionTimeline> nextExprFuture = null;
                    if (i + 1 < segments.size()) {
                        String nextSegment = segments.get(i + 1);
                        nextPcm = synthesize(synth, pcmOut, nextSegment);
                        long nextDurationMs = YukkuriSpeechPlayer.computeDurationMs(nextPcm, format);
                        nextExprFuture = acpExecutor.submit(
                                () -> analyzer.analyze(nextSegment, nextDurationMs));
                    }

                    player.play(currentPcm, format, currentExprFuture.get());
                    currentPcm = nextPcm;
                    currentExprFuture = nextExprFuture;
                }
            }
        }

        synth.deallocate();
    }

    private static byte[] synthesize(Synthesizer synth, ByteArrayOutputStream pcmOut, String text)
            throws InterruptedException {
Debug.print(text);
        pcmOut.reset();
        synth.speak(text, null);
        synth.waitEngineState(Synthesizer.QUEUE_EMPTY);
        return pcmOut.toByteArray();
    }
}
