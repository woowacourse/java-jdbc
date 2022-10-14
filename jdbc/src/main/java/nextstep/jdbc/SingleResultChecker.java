package nextstep.jdbc;

import java.util.List;

public class SingleResultChecker {

    public static void checkSingleResult(final List<Object> results) {
        if (results.size() != 1) {
            throw new IllegalStateException();
        }
    }

    private SingleResultChecker() {

    }
}
