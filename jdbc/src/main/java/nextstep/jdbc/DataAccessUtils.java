package nextstep.jdbc;

import java.util.Collection;

public class DataAccessUtils {
    public static <T> T getSingleResult(final Collection<T> results) {
        return results.iterator().next();
    }
}
