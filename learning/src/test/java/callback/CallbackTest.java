package callback;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CallbackTest {

    private static final Logger log = LoggerFactory.getLogger(CallbackTest.class);

    @Test
    void callback() {
        final SimpleTask task = new SimpleTask();
        Callback callback = () -> log.info("I'm done now.");
        assertDoesNotThrow(() -> task.executeWith(callback));
    }
}
