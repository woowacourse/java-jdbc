package nextstep.mvc.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import samples.TestController;

class ControllerScannerTest {

    @DisplayName("@Controller 가 붙은 클래스를 찾을 수 있다.")
    @Test
    void getControllers() {
        // given
        final ControllerScanner controllerScanner = new ControllerScanner(new Reflections("samples"));

        // when
        final Map<Class<?>, Object> controllers = controllerScanner.getControllers();

        // then
        assertThat(controllers.keySet()).hasSize(1).contains(TestController.class);
        assertThat(controllers.get(TestController.class)).isInstanceOf(TestController.class);
    }

    @DisplayName("컨트롤러 클래스를 찾지 못하는 경우 해당 클래스로 인스턴스를 생성할 수 없다.")
    @Test
    void cannotFindController() {
        // given
        final ControllerScanner controllerScanner = new ControllerScanner(new Reflections("invalid"));

        // when
        final Map<Class<?>, Object> controllers = controllerScanner.getControllers();

        // then
        assertThat(controllers.keySet()).isEmpty();
    }
}
