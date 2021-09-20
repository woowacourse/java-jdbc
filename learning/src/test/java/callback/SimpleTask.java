package callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTask extends Task {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleTask.class);

    @Override
    public void execute() {
        LOG.info("Perform some important activity and after call the callback method.");
    }
}
