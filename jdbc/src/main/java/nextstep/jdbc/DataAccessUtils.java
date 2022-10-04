package nextstep.jdbc;

import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUtils {

    private static final Logger log = LoggerFactory.getLogger(DataAccessUtils.class);

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
