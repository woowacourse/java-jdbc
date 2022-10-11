package nextstep.jdbc;

import java.util.List;

public class SingleResultChecker {

    public static <T> T checkSingleResult(final List<T> results) {
        if (results.size() != 1) {
            throw new IllegalStateException();
        }
        return results.get(0);
    }

    private SingleResultChecker() {

    }
}
