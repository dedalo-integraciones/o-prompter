package com.dedalo.oprompter.modern;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser {

    public static SpannableString parse(String text) {
        List<SpanInfo> spans = new ArrayList<SpanInfo>();

        findAndMarkSpans(text, "\\*([^*]+)\\*", spans, new StyleSpan(Typeface.BOLD));
        findAndMarkSpans(text, "_([^_]+)_", spans, new StyleSpan(Typeface.ITALIC));
        findAndMarkSpans(text, "\\+([^+]+)\\+", spans, new UnderlineSpan());
        findAndMarkSpans(text, "~([^~]+)~", spans, new StrikethroughSpan());

        String cleanText = removeMarkdownSymbols(text);
        SpannableString spannable = new SpannableString(cleanText);

        applySpansToCleanText(text, cleanText, spans, spannable);

        return spannable;
    }

    private static void findAndMarkSpans(String text, String regex, List<SpanInfo> spans, Object span) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            spans.add(new SpanInfo(matcher.start(1), matcher.end(1), span));
        }
    }

    private static String removeMarkdownSymbols(String text) {
        String result = text;
        result = result.replaceAll("\\*([^*]+)\\*", "$1");
        result = result.replaceAll("_([^_]+)_", "$1");
        result = result.replaceAll("\\+([^+]+)\\+", "$1");
        result = result.replaceAll("~([^~]+)~", "$1");
        return result;
    }

    private static void applySpansToCleanText(String originalText, String cleanText, List<SpanInfo> spans, SpannableString spannable) {
        for (SpanInfo spanInfo : spans) {
            String markedText = originalText.substring(spanInfo.start, spanInfo.end);
            int cleanStart = cleanText.indexOf(markedText);
            if (cleanStart != -1) {
                int cleanEnd = cleanStart + markedText.length();
                spannable.setSpan(spanInfo.span, cleanStart, cleanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static class SpanInfo {
        int start;
        int end;
        Object span;

        SpanInfo(int start, int end, Object span) {
            this.start = start;
            this.end = end;
            this.span = span;
        }
    }
}