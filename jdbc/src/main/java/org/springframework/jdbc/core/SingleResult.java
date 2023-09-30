package org.springframework.jdbc.core;

import java.util.List;
import java.util.Optional;

public class SingleResult {

    private static final int RESULT_INDEX = 0;

    public static <T> Optional<T> from(final List<T> results) {
        validateMultipleResult(results);
        return extractSingleResult(results);
    }

    private static <T> void validateMultipleResult(final List<T> results) {
        if (results.size() > 1) {
            throw new IllegalArgumentException("조회 결과가 2개 이상입니다.");
        }
    }

    private static <T> Optional<T> extractSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(RESULT_INDEX));
    }
}
