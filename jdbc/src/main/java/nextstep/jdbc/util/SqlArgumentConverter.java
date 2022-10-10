package nextstep.jdbc.util;

import java.time.temporal.Temporal;

public class SqlArgumentConverter {

    public static String convertObjectToString(final Object object) {
        if ((object instanceof String) || (object instanceof Temporal)) {
            return '\'' + object.toString() + '\'';
        }
        return String.valueOf(object);
    }
}
