package nextstep.mvc.handler.mapping;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import samples.TestController;

class ControllerScannerTest {

	@DisplayName("@Controller 클래스를 찾는다.")
	@Test
	void getControllers() {
		// given
		String basePackage = "samples";

		// when
		List<Class<?>> controllers = ControllerScanner.getControllers(basePackage);

		// then
		assertThat(controllers)
			.containsExactly(TestController.class);
	}
}
