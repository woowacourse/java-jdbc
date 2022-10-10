package nextstep.jdbc;

import java.text.MessageFormat;
import java.util.List;

public class DataAccessUtils {

    public static <T> T singleResult(final List<T> results) {
        validateSize(results);
        return results.iterator().next();
    }

    private static <T> void validateSize(final List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException("expect size is 1 but results size is 0");
        }

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(
                    MessageFormat.format("expect size is 1 but results size is {0}", results.size()));
        }
    }
}
