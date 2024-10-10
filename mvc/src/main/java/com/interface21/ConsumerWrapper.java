package com.interface21;

import java.util.function.Consumer;

public class ConsumerWrapper {

    public static <T> Consumer<T> accept(ThrowingConsumer<T, Exception> consumer) {
        return i -> {
            try {
                consumer.accept(i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private ConsumerWrapper() {}
}
