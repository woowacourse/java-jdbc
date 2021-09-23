package nextstep.util;

import exception.DataAccessException;
import exception.IncorrectDataSizeException;
import java.util.List;
import java.util.Objects;

public class DataAccessUtils {

    public static <T> T singleResult(List<T> result) {
        if (Objects.isNull(result)) {
            throw new DataAccessException("Result is null");
        }
        if (result.size() != 1) {
            throw new IncorrectDataSizeException(1, result.size());
        }

        return result.get(0);
    }
}
