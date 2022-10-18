package nextstep.jdbc.support;

public class StringUtils {

    public static void notBlank(final String str, final String name) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException(name + " must be not blank.");
        }
    }
}
