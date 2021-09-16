package templatemethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HitAndRunMethod extends StealingMethod {

    private static final Logger log = LoggerFactory.getLogger(HitAndRunMethod.class);

    @Override
    protected String pickTarget() {
        return "old goblin man";
    }

    @Override
    protected void confuseTarget(String target) {
        log.info("Approach the {} from behind.", target);
    }

    @Override
    protected void stealTheItem(String target) {
        log.info("Grab the wallet and run away fast!");
    }
}
