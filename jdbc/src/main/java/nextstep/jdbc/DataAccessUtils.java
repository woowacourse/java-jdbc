package nextstep.jdbc;

import java.util.List;

public class DataAccessUtils {

    public static <T> T singleResult(final List<T> results) {
        if (results.size() != 1) {
            throw new DataAccessException();
        }
        return results.iterator().next();
    }
}
