package nextstep.jdbc;

import java.util.Collection;
import java.util.Optional;

public class DataAccessUtils<T> {

    public static <T> Optional<T> optionalSingleResult(Collection<T> results) {
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DataAccessException("조회된 데이터의 개수가 1개를 초과합니다.");
        }
        return Optional.of(results.iterator().next());
    }
}
