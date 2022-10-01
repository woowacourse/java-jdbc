package nextstep.jdbc;

import java.util.function.IntConsumer;

public final class IntConsumerWrapper {

    public static IntConsumer accept(final ThrowingConsumer<Integer, Exception> consumer) {
        return i -> {
            try {
                consumer.accept(i);
            } catch (Exception e) {
                throw new DataAccessException();
            }
        };
    }
}
