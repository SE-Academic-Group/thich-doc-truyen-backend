package com.hcmus.group11.novelaggregator.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class UnicodeRemover {
    public static String removeUnicode(String input) {
        // Normalize the input string to the decomposed form
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Use a regex pattern to remove all combining diacritical marks
        Pattern pattern = Pattern.compile("\\p{M}");
        String withoutDiacritics = pattern.matcher(normalized).replaceAll("");

        return withoutDiacritics.replaceAll("[:?*\"<>|]", "");
    }
}
