package nextstep.jdbc.utils;

import java.util.Collection;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;

public class DataAccessUtils {

    public static <T> T nullableSingleResult(Collection<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException("데이터 조회 결과가 비어있습니다.");
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("데이터 조회 결과 크기가 올바르지 않습니다.");
        }
        return results.iterator().next();
    }
}
