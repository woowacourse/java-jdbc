package templatemethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StealingMethod {

    private static final Logger LOG = LoggerFactory.getLogger(StealingMethod.class);

    protected abstract String pickTarget();

    protected abstract void confuseTarget(String target);

    protected abstract void stealTheItem(String target);

    public final void steal() {
        final String target = pickTarget();
        LOG.info("The target has been chosen as {}.", target);
        confuseTarget(target);
        stealTheItem(target);
    }
}
