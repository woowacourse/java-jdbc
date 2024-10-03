package com.interface21.jdbc.support;

import com.interface21.jdbc.IncorrectResultSizeDataAccessException;
import java.util.Collection;
import javax.annotation.Nullable;

public class DataAccessUtils {

    private DataAccessUtils() {
    }

    public static <T> T nullableSingleResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
        if (results == null || results.isEmpty()) {
            throw new IncorrectResultSizeDataAccessException("조회 결과가 없습니다.");
        } else if (results.size() > 1) {
            String message = String.format("%d개의 결과를 예상했지만 %d개의 결과가 조회되었습니다.", 1, results.size());
            throw new IncorrectResultSizeDataAccessException(message);
        } else {
            return results.iterator().next();
        }
    }
}
