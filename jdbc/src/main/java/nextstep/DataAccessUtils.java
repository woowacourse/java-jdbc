package nextstep;

import java.util.List;
import nextstep.exception.DataAccessException;

public class DataAccessUtils {

    public static <T> T nullableSingleResult(List<T> results) {
        if(results.isEmpty()) {
            throw new DataAccessException("쿼리 결과가 존재하지 않습니다.");
        }
        if (results.size() > 1) {
            throw new DataAccessException("쿼리 결과가 2개 이상입니다.");
        }
        return results.iterator().next();
    }
}
