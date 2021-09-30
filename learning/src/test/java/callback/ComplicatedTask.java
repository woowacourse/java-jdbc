package callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComplicatedTask extends Task {

    private static final Logger log = LoggerFactory.getLogger(ComplicatedTask.class);

    @Override
    public void execute() {
        log.info("더하기를 시한다");
    }
}
