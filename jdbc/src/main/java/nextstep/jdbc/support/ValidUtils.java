package nextstep.jdbc.support;

public class ValidUtils {

    public static <T> void notNull(final T object, final String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " must be not null.");
        }
    }

    public static void notBlank(final String str, final String name) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException(name + " must be not blank.");
        }
    }
}
