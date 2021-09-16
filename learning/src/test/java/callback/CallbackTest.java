package callback;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CallbackTest {

    private static final Logger log = LoggerFactory.getLogger(CallbackTest.class);

    @Test
    void callback() {
        final SimpleTask task = new SimpleTask();
        task.executeWith(() -> log.info("I'm done now."));
    }
}
