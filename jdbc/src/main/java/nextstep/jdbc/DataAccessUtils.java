package nextstep.jdbc;

import java.util.List;

public class DataAccessUtils {

    public static <T> T singleResult(final List<T> results) {
        if (results.isEmpty() || results.size() > 2) {
            throw new DataAccessException();
        }
        return results.iterator().next();
    }
}
