package nextstep.util;

import javax.annotation.Nullable;

public final class Assert {

    private Assert() {
    }

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
