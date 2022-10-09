package nextstep.jdbc;

import java.util.List;

public class DataAccessUtils {

    public static <T> T nullableSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("Result is Empty");
        }
        if (results.size() > 1) {
            throw new DataAccessException("Result size is not only one");
        }
        return results.get(0);
    }

    private DataAccessUtils() {
    }
}
