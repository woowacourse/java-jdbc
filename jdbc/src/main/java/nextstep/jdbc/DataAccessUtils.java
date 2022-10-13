package nextstep.jdbc;

import java.util.List;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

class DataAccessUtils {

    public static <T> T nullableSingleResult(final List<T> result) {
        if (result.size() != 1) {
            throw new IncorrectResultSizeDataAccessException(1, result.size());
        }
        return result.iterator()
                .next();
    }
}
