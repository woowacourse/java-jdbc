package nextstep.jdbc.support;

public class ObjectUtils {

    public static <T> void notNull(final T object, final String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " must be not null.");
        }
    }
}
