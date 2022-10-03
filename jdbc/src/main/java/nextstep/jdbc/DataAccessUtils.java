package nextstep.jdbc;

import java.util.Collection;
import nextstep.jdbc.exception.EmptyResultException;
import nextstep.jdbc.exception.InvalidResultSizeException;

public class DataAccessUtils {
    public static <T> T getSingleResult(final Collection<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultException();
        }
        if (results.size() > 1) {
            throw new InvalidResultSizeException();
        }
        return results.iterator().next();
    }
}
