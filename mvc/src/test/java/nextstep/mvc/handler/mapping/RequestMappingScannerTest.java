package nextstep.mvc.handler.mapping;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import samples.TestController;

class RequestMappingScannerTest {

	@DisplayName("@RequestMapping handler를 찾는다.")
	@Test
	void getHandler() throws NoSuchMethodException {
		// given
		Class<TestController> clazz = TestController.class;

		// when
		Set<Method> handlers = RequestMappingScanner.getHandler(clazz);

		// then
		Class<?>[] params = {HttpServletRequest.class, HttpServletResponse.class};

		Method findUserId = TestController.class.getMethod("findUserId", params);
		Method save = TestController.class.getMethod("save", params);
		Method multiMethod = TestController.class.getMethod("multiMethod", params);

		assertThat(handlers)
			.containsOnly(findUserId, save, multiMethod);
	}
}
