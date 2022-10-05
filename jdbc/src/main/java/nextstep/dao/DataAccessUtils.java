package nextstep.dao;

import java.util.Collection;
import nextstep.dao.exception.DataAccessException;
import nextstep.dao.exception.EmptyResultDataAccessException;
import nextstep.dao.exception.IncorrectResultSizeDataAccessException;

public class DataAccessUtils {

    public static <T> T nullableSingleResult(Collection<T> results) throws DataAccessException {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException();
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException();
        }
        return results.iterator().next();
    }

    private DataAccessUtils() {
    }
}
