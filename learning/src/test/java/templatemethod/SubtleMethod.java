package templatemethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubtleMethod extends StealingMethod {

    private static final Logger log = LoggerFactory.getLogger(SubtleMethod.class);

    @Override
    protected String pickTarget() {
        return "shop keeper";
    }

    @Override
    protected void confuseTarget(String target) {
        log.info("Approach the {} with tears running and hug him!", target);
    }

    @Override
    protected void stealTheItem(String target) {
        log.info("While in close contact grab the {}'s wallet.", target);
    }
}
