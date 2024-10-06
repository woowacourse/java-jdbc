package com.interface21.dao;

import java.util.List;
import java.util.Optional;

public class DataAccessUtils {

    public static <T> Optional<T> optionalResult(List<T> results) throws IncorrectResultSizeDataAccessException {
        return Optional.ofNullable(singleResult(results));
    }

    private static <T> T singleResult(List<T> results) throws IncorrectResultSizeDataAccessException {
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.get(0);
    }
}
