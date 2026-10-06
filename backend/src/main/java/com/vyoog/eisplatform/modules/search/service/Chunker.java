package com.vyoog.eisplatform.modules.search.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits documentation into overlapping passages for semantic search
 * (REQ-PRT-003 BR-SEM-002). Text is split into sentences; sentences are
 * packed into chunks of at most {@code chunkSize} characters; each new chunk
 * starts with the last sentences of the previous one, up to {@code overlap}
 * characters. Every chunk starts with the document title, so a passage keeps
 * its context. A single sentence longer than {@code chunkSize} is cut.
 */
public final class Chunker {

    private Chunker() {
    }

    public static List<String> chunk(String title, String text, int chunkSize, int overlap) {
        String prefix = title == null || title.isBlank() ? "" : title.trim() + "\n";
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            chunks.add(prefix.trim());
            return chunks;
        }
        List<String> sentences = sentences(text, chunkSize);
        List<String> current = new ArrayList<>();
        int length = 0;
        for (String sentence : sentences) {
            if (!current.isEmpty() && length + sentence.length() + 1 > chunkSize) {
                chunks.add(prefix + String.join(" ", current));
                List<String> carried = new ArrayList<>();
                int carriedLength = 0;
                for (int i = current.size() - 1; i >= 0; i--) {
                    String previous = current.get(i);
                    if (carriedLength + previous.length() + 1 > overlap) {
                        break;
                    }
                    carried.add(0, previous);
                    carriedLength += previous.length() + 1;
                }
                current = carried;
                length = carriedLength;
            }
            current.add(sentence);
            length += sentence.length() + 1;
        }
        if (!current.isEmpty()) {
            chunks.add(prefix + String.join(" ", current));
        }
        return chunks;
    }

    private static List<String> sentences(String text, int maxLength) {
        List<String> out = new ArrayList<>();
        for (String part : text.replaceAll("\\s+", " ").trim().split("(?<=[.!?¿¡:;])\\s+")) {
            String sentence = part.trim();
            while (sentence.length() > maxLength) {
                int cut = sentence.lastIndexOf(' ', maxLength);
                cut = cut <= 0 ? maxLength : cut;
                out.add(sentence.substring(0, cut).trim());
                sentence = sentence.substring(cut).trim();
            }
            if (!sentence.isEmpty()) {
                out.add(sentence);
            }
        }
        return out;
    }
}
