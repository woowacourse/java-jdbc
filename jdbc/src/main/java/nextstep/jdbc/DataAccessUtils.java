package nextstep.jdbc;

import java.util.Collection;

public class DataAccessUtils {

    public static <T> T nullableSingleResult(Collection<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("결과값이 비어있습니다.");
        }
        if (results.size() > 1) {
            throw new DataAccessException("잘못된 개수의 결과값이 반환되었습니다.");
        }
        return results.iterator().next();
    }
}
