package nextstep.utils;

import java.util.Collection;
import nextstep.jdbc.DataAccessException;

public class DataAccessUtils {

    private static final int RESULT_SIZE_CRITERIA = 1;

    public static <T> T nullableSingleResult(final Collection<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("Empty Result");
        }
        if (results.size() > RESULT_SIZE_CRITERIA) {
            throw new DataAccessException("Not Single Result");
        }
        return results.iterator().next();
    }
}
