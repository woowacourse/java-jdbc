package nextstep.jdbc;

import java.util.List;
import nextstep.jdbc.exception.InvalidSizeResultException;
import nextstep.jdbc.exception.NotFoundResultException;

public class DataAccessUtils {

    private DataAccessUtils() {
    }

    public static <T> T nullableSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new NotFoundResultException("Result is empty");
        }
        if (results.size() > 1) {
            throw new InvalidSizeResultException("Result size is not only one");
        }
        return results.get(0);
    }
}
