package com.bng.ussd.util;

import java.util.Comparator;
import java.util.Set;

public class PatternMatcher {

    /**
     * Finds the best matching pattern for a given input string based on a defined priority.
     *
     * @param patterns A set of custom patterns.
     * @param input    The input string to match against.
     * @return The highest-priority matching pattern, or null if no match is found.
     */
    public static String matchPattern(Set<String> patterns, String input) {
        return patterns.stream()
                .filter(pattern -> matchesPattern(pattern, input))
                .min(Comparator.comparingInt(PatternMatcher::getPatternPriority))
                .orElse(null);
    }

    /**
     * Converts a custom pattern string into a standard regular expression and checks if the input matches.
     *
     * @param pattern The custom pattern (e.g., "530*_*#*9").
     * @param input   The input string.
     * @return True if the input matches the pattern, false otherwise.
     */
    private static boolean matchesPattern(String pattern, String input) {
        try {
            String regex = convertPatternToRegex(pattern);
            return input.matches(regex);
        } catch (Exception e) {
            // In case of an invalid pattern that leads to a bad regex
            System.err.println("Error compiling regex for pattern: " + pattern);
            return false;
        }
    }

    /**
     * Translates our custom pattern syntax into a valid Java regular expression.
     * - Literal '*' is escaped to '\*'.
     * - '_' (one numeric part) is translated to '(\d+)'.
     * - '#' (one or more numeric parts) is translated to '(\d+(?:\*\d+)*)'.
     *
     * @param pattern The custom pattern string.
     * @return A string representing the equivalent regular expression.
     */
    private static String convertPatternToRegex(String pattern) {
        String[] parts = pattern.split("\\*");
        String[] regexParts = new String[parts.length];

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if ("-".equals(part)) {
                // Matches one or more digits (a single numeric part)
                regexParts[i] = "\\d+";
            } else if ("~".equals(part)) {
                // Matches one numeric part, followed by zero or more groups of (* + numeric part)
                regexParts[i] = "\\d+(?:\\*\\d+)*";
            } else {
                // It's a literal number, no change needed
                regexParts[i] = part;
            }
        }
        
        // Join the parts with the escaped literal '*' delimiter and anchor to the start/end of the string
        return "^" + String.join("\\*", regexParts) + "$";
    }

    /**
     * Assigns a priority score to a pattern. A lower score means higher priority.
     * This helps decide which pattern to return if an input matches multiple patterns.
     *
     * @param pattern The pattern to score.
     * @return An integer representing the priority.
     */
    private static int getPatternPriority(String pattern) {
        boolean hasUnderscore = pattern.contains("-");
        boolean hasHash = pattern.contains("~");

        if (!hasUnderscore && !hasHash) {
            return 1; // Priority 1: Exact match (most specific)
        }
        if (hasUnderscore && !hasHash) {
            return 2; // Priority 2: Underscore patterns
        }
        if (hasUnderscore && hasHash) {
            return 3; // Priority 3: Hybrid patterns are more specific than hash-only
        }
        if (hasHash && !pattern.endsWith("~")) {
            return 4; // Priority 4: Complex hash patterns (e.g., "...#*9")
        }
        if (hasHash) {
            return 5; // Priority 5: Simple hash patterns (e.g., "...#") (least specific)
        }
        
        return Integer.MAX_VALUE; // Should not be reached
    }

//    public static void main(String[] args) {
//        // Expanded set of patterns to test more complex cases
////        Set<String> patterns = Set.of(
////            "530*6*1*_",          // Underscore
////            "530*6*1*#",          // Simple Hash
////            "530*6*1*1*2",      // Exact
////            "530*6*#",            // Simple Hash
////            "530*6*1",            // Exact
////            "530*6*#*9",          // Complex Hash
////            "530*_*_*9",        // **NEW**: Multiple Underscores
////            "530*_*#*9"         // **NEW**: Hybrid Underscore and Hash
////        );
////
////        System.out.println("--- Basic Tests ---");
////        System.out.println("Input: 530*6*1*1*2 -> Matched: " + matchPattern(patterns, "530*6*1*1*2"));
////        System.out.println("Input: 530*6*1*99 -> Matched: " + matchPattern(patterns, "530*6*1*99"));
////        System.out.println("Input: 530*6*1*4*5*6 -> Matched: " + matchPattern(patterns, "530*6*1*4*5*6"));
////        System.out.println("Input: 530*6*3*5*8*9 -> Matched: " + matchPattern(patterns, "530*6*3*5*8*9"));
////
////        System.out.println("\n--- Advanced Generic Tests ---");
////        System.out.println("Input: 530*12*34*9 -> Matched: " + matchPattern(patterns, "530*12*34*9")); // Should match 530*_*_*9
////        System.out.println("Input: 530*1*2*3*4*9 -> Matched: " + matchPattern(patterns, "530*1*2*3*4*9")); // Should match 530*_*#*9
////        System.out.println("Input: 530*99*9 -> Matched: " + matchPattern(patterns, "530*99*9")); // Should NOT match 530*_*_*9 (not enough parts)
//    
//    
//        String[] pattern = {"530*6*1*_", "530*6*1*#", "530*6*1*1*2", "530*6*#", "530*6*1", "530*6*#*9"};
//        Set<String> patterns = Arrays.stream(pattern)
//                .collect(Collectors.toSet());
//        // Test cases
//        System.out.println(matchPattern(patterns, "530*6*1*1*2"));    // Expected: 530*6*1*1*2 (exact match)
//        System.out.println(matchPattern(patterns, "530*6*1*1"));      // Expected: 530*6*1*_ (underscore pattern)
//        System.out.println(matchPattern(patterns, "530*6*1*99"));     // Expected: 530*6*1*_ (underscore pattern)
//        System.out.println(matchPattern(patterns, "530*6*1*4*5*6"));  // Expected: 530*6*1*# (hash pattern)
//        System.out.println(matchPattern(patterns, "530*6*1"));        // Expected: 530*6*1
//        System.out.println(matchPattern(patterns, "530*6*7*55*33*44*22"));    // Expected: 530*6*#
//        System.out.println(matchPattern(patterns, "530*6*3*5*8*9"));    // Expected: 530*6*#*9
//    }
}