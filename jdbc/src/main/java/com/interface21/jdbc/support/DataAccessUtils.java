package com.interface21.jdbc.support;

import com.interface21.jdbc.IncorrectResultSizeDataAccessException;
import java.util.Collection;
import javax.annotation.Nullable;

public class DataAccessUtils {

    private DataAccessUtils() {
    }

    public static <T> T nullableSingleResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
        if (results == null || results.isEmpty()) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.iterator().next();
    }
}
