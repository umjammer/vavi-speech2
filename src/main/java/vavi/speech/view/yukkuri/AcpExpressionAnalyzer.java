/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.agentclientprotocol.sdk.client.AcpClient;
import com.agentclientprotocol.sdk.client.AcpSyncClient;
import com.agentclientprotocol.sdk.client.transport.AgentParameters;
import com.agentclientprotocol.sdk.client.transport.StdioAcpClientTransport;
import com.agentclientprotocol.sdk.spec.AcpSchema.AgentMessageChunk;
import com.agentclientprotocol.sdk.spec.AcpSchema.NewSessionRequest;
import com.agentclientprotocol.sdk.spec.AcpSchema.NewSessionResponse;
import com.agentclientprotocol.sdk.spec.AcpSchema.PromptRequest;
import com.agentclientprotocol.sdk.spec.AcpSchema.TextContent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * {@link ExpressionAnalyzer} backed by an Agent Client Protocol (ACP) agent
 * (defaulting to {@code opencode acp} over stdio).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class AcpExpressionAnalyzer implements ExpressionAnalyzer, AutoCloseable {

    private static final Logger logger = Logger.getLogger(AcpExpressionAnalyzer.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AgentParameters agentParameters;
    private final String workingDirectory;
    private final Duration promptTimeout;
    private final boolean fallbackToHeuristic;
    private final AtomicReference<StringBuilder> activeResponse = new AtomicReference<>();
    private AcpSyncClient client;

    /**
     * Creates an analyzer that spawns {@code opencode acp} in the current working directory.
     */
    public AcpExpressionAnalyzer() {
        this(
                AgentParameters.builder("opencode").arg("acp").build(),
                Path.of("").toAbsolutePath().toString(),
                Duration.ofSeconds(60),
                true
        );
    }

    public AcpExpressionAnalyzer(
            AgentParameters agentParameters,
            String workingDirectory,
            Duration promptTimeout,
            boolean fallbackToHeuristic) {
        this.agentParameters = Objects.requireNonNull(agentParameters, "agentParameters");
        this.workingDirectory = Objects.requireNonNull(workingDirectory, "workingDirectory");
        this.promptTimeout = Objects.requireNonNull(promptTimeout, "promptTimeout");
        this.fallbackToHeuristic = fallbackToHeuristic;
    }

    @Override
    public ExpressionTimeline analyze(String text, long durationMs) throws IOException {
        if (text == null || text.isBlank() || durationMs <= 0) {
            return ExpressionTimeline.empty();
        }

        try {
            String rawResponse = queryAcp(buildPrompt(text));
            ExpressionTimeline timeline = parseResponse(rawResponse, durationMs);
            if (!timeline.entries().isEmpty()) {
                return timeline;
            }
            logger.warning("ACP returned empty expression timeline; falling back if enabled");
        } catch (Exception e) {
            if (!fallbackToHeuristic) {
                throw new IOException("Failed to analyze facial expressions via ACP", e);
            }
            logger.log(Level.WARNING, "ACP expression analysis failed, using heuristic fallback: " + e.getMessage(), e);
        }

        return fallbackToHeuristic
                ? new HeuristicExpressionAnalyzer().analyze(text, durationMs)
                : ExpressionTimeline.empty();
    }

    /**
     * Determines the default {@link FacialExpression} for the given text via ACP,
     * defaulting to {@link FacialExpression#HAPPY} when blank or undetermined.
     */
    public FacialExpression analyzeDefaultExpression(String text) throws IOException {
        if (text == null || text.isBlank()) {
            return FacialExpression.HAPPY;
        }

        try {
            String rawResponse = queryAcp(buildDefaultExpressionPrompt(text));
            FacialExpression expr = parseDefaultExpressionResponse(rawResponse);
            if (expr != null) {
                return expr;
            }
            logger.warning("ACP returned empty default expression; falling back if enabled");
        } catch (Exception e) {
            if (!fallbackToHeuristic) {
                throw new IOException("Failed to determine default facial expression via ACP", e);
            }
            logger.log(Level.WARNING, "ACP default expression analysis failed, using heuristic fallback: " + e.getMessage(), e);
        }

        if (fallbackToHeuristic) {
            FacialExpression heuristic = HeuristicExpressionAnalyzer.classify(text);
            return heuristic == FacialExpression.NEUTRAL ? FacialExpression.HAPPY : heuristic;
        }
        return FacialExpression.HAPPY;
    }

    static String buildDefaultExpressionPrompt(String voiceText) {
        return """
                Analyze the overall emotional tone of the following text and choose the single best default facial expression.
                Return ONLY one of the following exact names (no markdown, no explanation):
                NEUTRAL, HAPPY, EXCITED, ANGRY, FURIOUS, SAD, SURPRISED, SMUG, PALE, DARK, WINK

                Text:
                """ + voiceText;
    }

    /**
     * Parses an ACP response into a single {@link FacialExpression},
     * returning {@code null} when the response is blank or unrecognized.
     */
    public static FacialExpression parseDefaultExpressionResponse(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }
        String trimmed = response.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);
        for (FacialExpression expr : FacialExpression.values()) {
            if (upper.equals(expr.name())) {
                return expr;
            }
        }
        for (FacialExpression expr : FacialExpression.values()) {
            if (upper.contains(expr.name())) {
                return expr;
            }
        }
        FacialExpression mapped = FacialExpression.fromString(trimmed);
        return mapped != FacialExpression.NEUTRAL ? mapped : null;
    }

    private AcpSyncClient ensureClient() {
        if (client == null) {
            StdioAcpClientTransport transport = new StdioAcpClientTransport(agentParameters);
            AcpSyncClient newClient = AcpClient.sync(transport)
                    .requestTimeout(promptTimeout)
                    .promptTimeout(promptTimeout)
                    .sessionUpdateHandler(notification -> {
                        StringBuilder sb = activeResponse.get();
                        if (sb != null
                                && notification.update() instanceof AgentMessageChunk chunk
                                && chunk.content() instanceof TextContent tc) {
                            sb.append(tc.text());
                        }
                    })
                    .build();
            newClient.initialize();
            client = newClient;
        }
        return client;
    }

    protected synchronized String queryAcp(String prompt) {
        StringBuilder responseText = new StringBuilder();
        activeResponse.set(responseText);
        try {
            AcpSyncClient c = ensureClient();
            NewSessionResponse session = c.newSession(new NewSessionRequest(workingDirectory));
            c.prompt(PromptRequest.text(session.sessionId(), prompt));
            return responseText.toString();
        } catch (RuntimeException e) {
            close();
            throw e;
        } finally {
            activeResponse.set(null);
        }
    }

    @Override
    public synchronized void close() {
        if (client != null) {
            try {
                client.close();
            } finally {
                client = null;
            }
        }
    }

    static String buildPrompt(String voiceText) {
        return """
                Analyze the emotional tone of the following spoken voice script and split it into a facial expression timeline.
                Return ONLY a valid JSON array (no markdown, no explanation).
                Each element must have:
                - "startRatio": number in [0.0, 1.0]
                - "endRatio": number in [0.0, 1.0]
                - "expression": one of [NEUTRAL, HAPPY, EXCITED, ANGRY, FURIOUS, SAD, SURPRISED, SMUG, PALE, DARK, WINK]
                - "segment": the corresponding phrase from the script

                Voice script:
                """ + voiceText;
    }

    record AcpTimelineItem(double startRatio, double endRatio, String expression, String segment) {
    }

    /**
     * Parses an ACP JSON response into an {@link ExpressionTimeline} scaled to {@code durationMs}.
     */
    public static ExpressionTimeline parseResponse(String response, long durationMs) throws IOException {
        if (response == null || response.isBlank() || durationMs <= 0) {
            return ExpressionTimeline.empty();
        }
        String json = extractJsonArray(response);
        if (json == null) {
            return ExpressionTimeline.empty();
        }
        List<AcpTimelineItem> items = MAPPER.readValue(json, new TypeReference<>() {});
        List<ExpressionTimeline.Entry> entries = new ArrayList<>();
        for (AcpTimelineItem item : items) {
            double start = Math.clamp(item.startRatio(), 0.0, 1.0);
            double end = Math.clamp(item.endRatio(), start, 1.0);
            long startMs = Math.round(start * durationMs);
            long endMs = Math.round(end * durationMs);
            FacialExpression expr = FacialExpression.fromString(item.expression());
            entries.add(new ExpressionTimeline.Entry(startMs, endMs, expr, item.segment()));
        }
        return new ExpressionTimeline(entries);
    }

    private static String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start < 0 || end < start) {
            return null;
        }
        return text.substring(start, end + 1);
    }
}
