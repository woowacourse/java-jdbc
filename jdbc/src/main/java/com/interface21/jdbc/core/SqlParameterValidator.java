package com.interface21.jdbc.core;

public class SqlParameterValidator {

    private SqlParameterValidator() {}

    public static void validate(String sql, Object... args) {
        int expectedCount = countPlaceholders(sql);
        if (args.length != expectedCount) {
            throw new IllegalArgumentException(
                    String.format("SQL 파라미터 개수 불일치: 기대값=%d, 실제값=%d",
                            expectedCount, args.length)
            );
        }
    }

    private static int countPlaceholders(String sql) {
        int count = 0;
        boolean inQuotes = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '\'') {
                inQuotes = !inQuotes;
            }

            if (c == '?' && !inQuotes) {
                count++;
            }
        }

        return count;
    }

}
