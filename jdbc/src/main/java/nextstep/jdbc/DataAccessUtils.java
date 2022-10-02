package nextstep.jdbc;

import java.util.Collection;
import nextstep.jdbc.exception.IllegalDataSizeException;

public class DataAccessUtils {

    private static final int ONE_DATA_SIZE = 1;

    public static <T> T nullableSingleResult(Collection<T> results) {
        if (results.isEmpty()) {
            return null;
        }

        if (results.size() > ONE_DATA_SIZE) {
            throw new IllegalDataSizeException();
        }

        return results.iterator().next();
    }
}
