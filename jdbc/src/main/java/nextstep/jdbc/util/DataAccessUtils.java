package nextstep.jdbc.util;

import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.NotSingleResultDataException;

import java.util.Collection;
import java.util.Objects;

public class DataAccessUtils {
    private DataAccessUtils() {
    }

    public static <T> T singleResult(Collection<T> results) throws DataAccessException {
        if (Objects.isNull(results) || results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new NotSingleResultDataException("Result size is " + results.size());
        }
        return results.iterator().next();
    }

    public static <T> T notNullSingleResult(Collection<T> results) throws DataAccessException {
        if (Objects.isNull(results) || results.isEmpty()) {
            throw new NotSingleResultDataException("Result is Empty");
        }
        if (results.size() > 1) {
            throw new NotSingleResultDataException("Result size is " + results.size());
        }
        return results.iterator().next();
    }
}
