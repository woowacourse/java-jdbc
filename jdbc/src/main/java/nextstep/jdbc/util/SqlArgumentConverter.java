package nextstep.jdbc.util;

public class SqlArgumentConverter {

    public static String convertObjectToString(final Object object) {
        if (object instanceof String) {
            return '\'' + (String) object + '\'';
        }
        return String.valueOf(object);
    }
}
