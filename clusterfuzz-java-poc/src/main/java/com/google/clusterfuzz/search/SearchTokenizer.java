package com.google.clusterfuzz.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for tokenizing strings for search indexing.
 * Converted from Python search_tokenizer utilities.
 */
public class SearchTokenizer {

    private static final String DELIMITER_PATTERN = "[\\s\\-_\\.@]+";
    private static final int MIN_TOKEN_LENGTH = 2;
    private static final int MAX_TOKEN_LENGTH = 50;

    /**
     * Tokenize a string into search tokens.
     * Splits on common delimiters and filters by length.
     */
    public static Set<String> tokenize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(input.toLowerCase().split(DELIMITER_PATTERN))
                .filter(token -> !token.isEmpty())
                .filter(token -> token.length() >= MIN_TOKEN_LENGTH)
                .filter(token -> token.length() <= MAX_TOKEN_LENGTH)
                .filter(SearchTokenizer::isValidToken)
                .collect(Collectors.toSet());
    }

    /**
     * Check if a token is valid for indexing.
     * Excludes purely numeric tokens and common stop words.
     */
    private static boolean isValidToken(String token) {
        // Exclude purely numeric tokens
        if (token.matches("\\d+")) {
            return false;
        }

        // Exclude common stop words
        Set<String> stopWords = Set.of(
            "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did",
            "will", "would", "could", "should", "may", "might", "can", "must"
        );
        
        return !stopWords.contains(token);
    }

    /**
     * Tokenize multiple strings and combine the results.
     */
    public static Set<String> tokenizeMultiple(String... inputs) {
        Set<String> allTokens = new HashSet<>();
        for (String input : inputs) {
            allTokens.addAll(tokenize(input));
        }
        return allTokens;
    }

    /**
     * Create search-friendly version of a string.
     * Useful for creating searchable versions of names, descriptions, etc.
     */
    public static String createSearchableString(String input) {
        if (input == null) {
            return "";
        }
        
        return tokenize(input).stream()
                .sorted()
                .collect(Collectors.joining(" "));
    }
}