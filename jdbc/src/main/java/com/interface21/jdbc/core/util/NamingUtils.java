package com.interface21.jdbc.core.util;

public final class NamingUtils {

    private NamingUtils() {
    }

    public static String snakeToCamel(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        StringBuilder result = new StringBuilder();
        boolean upperNext = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                upperNext = true;
                continue;
            }
            appendChar(result, c, upperNext);
            upperNext = false;
        }
        return result.toString();
    }

    private static void appendChar(StringBuilder builder, char c, boolean upperNext) {
        if (upperNext) {
            builder.append(Character.toUpperCase(c));
            return;
        }
        builder.append(c);
    }
}
