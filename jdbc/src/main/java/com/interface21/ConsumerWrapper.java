package com.interface21;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerWrapper {

    private static final Logger log = LoggerFactory.getLogger(ConsumerWrapper.class);

    public static <T> Consumer<T> accept(ThrowingConsumer<T> consumer) {
        return i -> {
            try {
                consumer.accept(i);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };
    }
}
