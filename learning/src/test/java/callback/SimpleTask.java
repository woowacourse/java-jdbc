package callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTask extends Task {

    private static final Logger log = LoggerFactory.getLogger(SimpleTask.class);

    @Override
    public void execute() {
        log.info("Perform some important activity and after call the callback method.");
    }
}
