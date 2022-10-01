package nextstep.jdbc;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUtils {

    private static final Logger log = LoggerFactory.getLogger(DataAccessUtils.class);

    public static <T> T nullableSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            log.error("Result is Empty");
            throw new DataAccessException();
        }
        if (results.size() > 1) {
            log.error("Result size is not only one");
            throw new DataAccessException();
        }
        return results.get(0);
    }
}
