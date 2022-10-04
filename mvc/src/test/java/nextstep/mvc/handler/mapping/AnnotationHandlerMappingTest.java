package nextstep.mvc.handler.mapping;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.handler.HandlerExecution;
import nextstep.mvc.view.ModelAndView;

class AnnotationHandlerMappingTest {

	private AnnotationHandlerMapping handlerMapping;

	@BeforeEach
	void setUp() {
		handlerMapping = new AnnotationHandlerMapping("samples");
		handlerMapping.initialize();
	}

	@Test
	void get() throws Exception {
		final var request = mock(HttpServletRequest.class);
		final var response = mock(HttpServletResponse.class);

		when(request.getAttribute("id")).thenReturn("gugu");
		when(request.getRequestURI()).thenReturn("/get-test");
		when(request.getMethod()).thenReturn("GET");

		final var handlerExecution = (HandlerExecution)handlerMapping.getHandler(request).get();
		final var modelAndView = (ModelAndView)handlerExecution.handle(request, response);

		assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
	}

	@Test
	void post() throws Exception {
		final var request = mock(HttpServletRequest.class);
		final var response = mock(HttpServletResponse.class);

		when(request.getAttribute("id")).thenReturn("gugu");
		when(request.getRequestURI()).thenReturn("/post-test");
		when(request.getMethod()).thenReturn("POST");

		final var handlerExecution = (HandlerExecution)handlerMapping.getHandler(request).get();
		final var modelAndView = (ModelAndView)handlerExecution.handle(request, response);

		assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
	}

	@Test
	void get_and_post_POST() throws Exception {
		final var request = mock(HttpServletRequest.class);
		final var response = mock(HttpServletResponse.class);

		when(request.getAttribute("id")).thenReturn("gugu");
		when(request.getRequestURI()).thenReturn("/get-and-post");
		when(request.getMethod()).thenReturn("POST");

		final var handlerExecution = (HandlerExecution)handlerMapping.getHandler(request).get();
		final var modelAndView = (ModelAndView)handlerExecution.handle(request, response);

		assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
	}

	@Test
	void get_and_post_GET() throws Exception {
		final var request = mock(HttpServletRequest.class);
		final var response = mock(HttpServletResponse.class);

		when(request.getAttribute("id")).thenReturn("gugu");
		when(request.getRequestURI()).thenReturn("/get-and-post");
		when(request.getMethod()).thenReturn("GET");

		final var handlerExecution = (HandlerExecution)handlerMapping.getHandler(request).get();
		final var modelAndView = (ModelAndView)handlerExecution.handle(request, response);

		assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
	}
}
