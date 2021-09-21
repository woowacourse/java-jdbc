package templatemethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayDownMethod extends StealingMethod{

    private static final Logger log = LoggerFactory.getLogger(LayDownMethod.class);

    @Override
    protected String pickTarget() {
        return "낮잠을 자고 있는사람";
    }

    @Override
    protected void confuseTarget(String target) {
        log.info("옆에서 일단 같이 누워있는 척을 합니다");
    }

    @Override
    protected void stealTheItem(String target) {
        log.info("곤히 자고있는걸 확인하면, 지갑을 훔치고 잠에 듭니다");
    }
}
