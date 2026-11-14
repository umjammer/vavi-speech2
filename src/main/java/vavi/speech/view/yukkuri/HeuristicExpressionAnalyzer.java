/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.speech.view.yukkuri;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Rule-based offline {@link ExpressionAnalyzer} that splits text into clauses
 * and classifies emotion from Japanese/English cues and punctuation.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class HeuristicExpressionAnalyzer implements ExpressionAnalyzer {

    private static final Pattern CLAUSE_PATTERN = Pattern.compile("[^。！？!?\\n]+[。！？!?\\n]*");

    @Override
    public ExpressionTimeline analyze(String text, long durationMs) {
        if (text == null || text.isBlank() || durationMs <= 0) {
            return ExpressionTimeline.empty();
        }

        List<String> clauses = new ArrayList<>();
        Matcher matcher = CLAUSE_PATTERN.matcher(text.trim());
        while (matcher.find()) {
            String clause = matcher.group().trim();
            if (!clause.isEmpty()) {
                clauses.add(clause);
            }
        }
        if (clauses.isEmpty()) {
            clauses.add(text.trim());
        }

        int totalChars = clauses.stream().mapToInt(String::length).sum();
        List<ExpressionTimeline.Entry> entries = new ArrayList<>();
        long cursor = 0;

        for (int i = 0; i < clauses.size(); i++) {
            String clause = clauses.get(i);
            long end = (i == clauses.size() - 1)
                    ? durationMs
                    : cursor + Math.round((double) durationMs * clause.length() / Math.max(1, totalChars));
            end = Math.max(cursor, Math.min(durationMs, end));
            entries.add(new ExpressionTimeline.Entry(cursor, end, classify(clause), clause));
            cursor = end;
        }

        return new ExpressionTimeline(entries);
    }

    static FacialExpression classify(String clause) {
        String lower = clause.toLowerCase();
        if (containsAny(lower, "激怒", "ふざけるな", "許さない", "furious")) {
            return FacialExpression.FURIOUS;
        }
        if (containsAny(lower, "怒", "むかつ", "イライラ", "バカ", "ばか", "angry", "mad")) {
            return FacialExpression.ANGRY;
        }
        if (containsAny(lower, "悲し", "泣", "つらい", "辛い", "寂し", "ごめん", "残念", "sad", "sorry")) {
            return FacialExpression.SAD;
        }
        if (containsAny(lower, "えっ", "まさか", "驚", "びっくり", "うそ", "嘘", "what?!", "surprised")) {
            return FacialExpression.SURPRISED;
        }
        if (containsAny(lower, "怖", "やば", "青ざめ", "蒼白", "ショック")) {
            return FacialExpression.PALE;
        }
        if (containsAny(lower, "やった", "最高", "すごい", "凄", "ワクワク", "きらきら", "excited", "awesome")) {
            return FacialExpression.EXCITED;
        }
        if (containsAny(lower, "嬉し", "うれし", "楽し", "たのし", "笑", "ありがとう", "好き", "happy", "glad", "♪")) {
            return FacialExpression.HAPPY;
        }
        if (containsAny(lower, "ふふ", "どや", "当然", "余裕", "smug")) {
            return FacialExpression.SMUG;
        }
        if (clause.endsWith("！") || clause.endsWith("!")) {
            return FacialExpression.EXCITED;
        }
        if (clause.endsWith("？") || clause.endsWith("?")) {
            return FacialExpression.SURPRISED;
        }
        return FacialExpression.NEUTRAL;
    }

    private static boolean containsAny(String target, String... keywords) {
        for (String keyword : keywords) {
            if (target.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
