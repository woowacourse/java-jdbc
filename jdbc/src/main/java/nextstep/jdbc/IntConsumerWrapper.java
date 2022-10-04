package nextstep.jdbc;

import java.util.function.IntConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntConsumerWrapper {

    private static final Logger log = LoggerFactory.getLogger(IntConsumerWrapper.class);

    public static IntConsumer accept(final ThrowingConsumer<Integer, Exception> consumer) {
        return i -> {
            try {
                consumer.accept(i);
            } catch (Exception e) {
                throw new RuntimeException("accept exception", e);
            }
        };
    }

    private IntConsumerWrapper() {
    }
}
