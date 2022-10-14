package nextstep.jdbc.util;

import java.time.temporal.Temporal;

public class SqlArgumentConverter {

    public static final String INVALID_TYPE = "Cannot convert Object to String.";

    public static String convertObjectToString(final Object object) {
        validateTargetObjectType(object);
        if ((object instanceof String) || (object instanceof Temporal)) {
            return '\'' + object.toString() + '\'';
        }
        return String.valueOf(object);
    }

    private static void validateTargetObjectType(final Object target) {
        if (!((target instanceof Short) || (target instanceof Integer) || (target instanceof Long)
                ||(target instanceof Double) || (target instanceof Float) || (target instanceof Boolean)
                || (target instanceof String) || (target instanceof Temporal) || (target instanceof Character)
                || (target instanceof Byte))) {
            throw new IllegalArgumentException(INVALID_TYPE);
        }
    }
}
