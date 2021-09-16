package templatemethod;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TemplateMethodTest {

    private static final Logger log = LoggerFactory.getLogger(TemplateMethodTest.class);

    @Test
    void steal() {
        final HalflingThief thief = new HalflingThief(new HitAndRunMethod());
        thief.steal();
        thief.changeMethod(new SubtleMethod());
        thief.steal();
    }

    @Test
    void anonymousClass() {
        final StealingMethod ghostMethod = new StealingMethod() {
            @Override
            protected String pickTarget() {
                return "ghost";
            }

            @Override
            protected void confuseTarget(String target) {
                log.info("It wasn't a {}; it was a real person.", target);
            }

            @Override
            protected void stealTheItem(String target) {
                log.info("Steal a real person's watch");
            }
        };

        ghostMethod.steal();
    }
}
