package nextstep.jdbc.utils;

import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;

import javax.annotation.Nullable;
import java.util.Collection;

public class DataAccessUtils {

    private DataAccessUtils() {
    }

    public static <T> T singleResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
        if (results == null || results.isEmpty()) {
            return null;
        }
        if (results.size() != 1) {
            throw new IncorrectResultSizeDataAccessException(results.size());
        }
        return results.iterator().next();
    }

    public static <T> T requiredSingleResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
        if (results == null || results.size() != 1) {
            throw new IncorrectResultSizeDataAccessException();
        }
        return results.iterator().next();
    }

}
