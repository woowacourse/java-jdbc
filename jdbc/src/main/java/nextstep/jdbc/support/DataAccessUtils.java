package nextstep.jdbc.support;

import java.util.List;
import nextstep.jdbc.exception.BlankException;
import nextstep.jdbc.exception.EmptyResultException;
import nextstep.jdbc.exception.IncorrectDataSizeException;
import nextstep.jdbc.exception.NullException;

public class DataAccessUtils {

    private static final int RESULT_SIZE_OF_ONE = 1;
    private static final int FIRST_INDEX_OF_RESULT = 0;

    public static <T> T singleResult(final List<T> result) {
        if (result.isEmpty()) {
            throw new EmptyResultException(RESULT_SIZE_OF_ONE);
        }
        if (result.size() > RESULT_SIZE_OF_ONE) {
            throw new IncorrectDataSizeException(RESULT_SIZE_OF_ONE, result.size());
        }
        return result.get(FIRST_INDEX_OF_RESULT);
    }

    public static <T> void notNull(final T object, final String name) {
        if (object == null) {
            throw new NullException(name);
        }
    }

    public static <T> void notBlank(final String object, final String name) {
        if (object == null || object.isBlank()) {
            throw new BlankException(name);
        }
    }
}
