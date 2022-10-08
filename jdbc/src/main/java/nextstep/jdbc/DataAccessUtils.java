package nextstep.jdbc;

import java.util.List;

public class DataAccessUtils {

    private DataAccessUtils() {
    }

    public static <T> T nullableSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("Result is empty");
        }
        if (results.size() > 1) {
            throw new DataAccessException("Result size is not only one");
        }
        return results.get(0);
    }
}
