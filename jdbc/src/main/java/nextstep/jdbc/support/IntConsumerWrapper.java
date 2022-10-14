package nextstep.jdbc.support;

import java.util.function.IntConsumer;

public class IntConsumerWrapper {

    private IntConsumerWrapper() {
    }

    public static IntConsumer accept(final ThrowingConsumer<Integer, Exception> consumer) {
        return i -> {
            try {
                consumer.accept(i);
            } catch (Exception e) {
                throw new RuntimeException("Accept exception", e);
            }
        };
    }
}
