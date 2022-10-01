package nextstep.jdbc.support;

import java.util.Collection;
import nextstep.jdbc.dao.EmptyResultDataAccessException;
import nextstep.jdbc.dao.IncorrectResultSizeDataAccessException;

public abstract class DataAccessUtils {

    public static <T> T singleResult(final Collection<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }

        return results.iterator().next();
    }
}
