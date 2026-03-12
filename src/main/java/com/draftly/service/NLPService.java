package com.draftly.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NLP Service - Handles keyword extraction, grammar checking, and summarization.
 * Used across UC1, UC3, UC6, UC8.
 */
@Service
public class NLPService {

    /**
     * Extract keywords from text using basic NLP techniques (TF-IDF / OpenNLP).
     * Used in UC1: Create Research Project.
     */
    public List<String> extractKeywords(String text) {
        // TODO: Implement with OpenNLP tokenizer + TF-IDF keyword extraction
        // Placeholder: simple word frequency-based extraction
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String[] words = text.toLowerCase().split("\\W+");
        return Arrays.stream(words)
                .filter(w -> w.length() > 4) // filter short words
                .distinct()
                .limit(10)
                .toList();
    }

    /**
     * Check grammar and highlight unclear sentences.
     * Used in UC6 and UC8.
     */
    public List<String> checkGrammar(String text) {
        // TODO: Implement grammar checking using OpenNLP or LanguageTool
        return new ArrayList<>();
    }

    /**
     * Summarize a block of text (e.g., abstract summarization).
     * Used in UC8.
     */
    public String summarize(String text, int maxSentences) {
        // TODO: Implement extractive summarization using sentence scoring
        return text;
    }
}
