package nextstep.jdbc;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAccessUtils {

    private static final Logger log = LoggerFactory.getLogger(DataAccessUtils.class);

    private DataAccessUtils() {
    }

    public static <T> T getSingleResult(final Collection<T> results) {
        if (results.isEmpty()) {
            log.error("데이터가 존재하지 않습니다.");
            throw new DataAccessException("데이터가 존재하지 않습니다.");
        }

        if (results.size() > 1) {
            log.error("result={}", results);
            throw new DataAccessException("데이터가 2개 이상 존재합니다.");
        }

        return results.iterator().next();
    }
}
