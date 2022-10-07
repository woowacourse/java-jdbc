package nextstep.jdbc.support;

import javax.annotation.Nullable;

public class Assert {

    private Assert() {
    }

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
