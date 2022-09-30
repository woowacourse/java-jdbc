package nextstep.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.handler.HandlerExecution;
import nextstep.mvc.handler.mapping.AnnotationHandlerMapping;
import samples.TestController;

class HandlerMappingRegistryTest {

	@DisplayName("url과 requestMethod에 해당하는 method를 찾는다.")
	@Test
	void getHandler() throws NoSuchMethodException {
		// given
		final var handlerMappingRegistry = HandlerMappingRegistry.builder()
			.add(new AnnotationHandlerMapping("samples"))
			.build();
		handlerMappingRegistry.initialize();

		final var request = mock(HttpServletRequest.class);

		when(request.getRequestURI()).thenReturn("/get-test");
		when(request.getMethod()).thenReturn("GET");

		// when
		Object handler = handlerMappingRegistry.getHandler(request);

		// then
		assertThat((HandlerExecution)handler)
			.extracting("method")
			.isEqualTo(TestController.class.getMethod(
				"findUserId", HttpServletRequest.class, HttpServletResponse.class)
			);
	}
}
