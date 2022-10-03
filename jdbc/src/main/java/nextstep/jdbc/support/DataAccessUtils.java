package nextstep.jdbc.support;

import java.util.List;

public abstract class DataAccessUtils {

    private DataAccessUtils() {
    }

    public static <T> T uniqueResult(List<T> results) {
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new RuntimeException("결과 값이 한 개 보다 많습니다.");
        }
        return results.iterator().next();
    }
}
