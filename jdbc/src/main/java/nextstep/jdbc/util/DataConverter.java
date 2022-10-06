package nextstep.jdbc.util;

public class DataConverter {

    public static String convertObjectToString(final Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        return String.valueOf(object);
    }
}
