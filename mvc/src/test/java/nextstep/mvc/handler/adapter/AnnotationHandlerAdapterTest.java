package nextstep.mvc.handler.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.mvc.handler.HandlerExecution;

class AnnotationHandlerAdapterTest {

	@DisplayName("AnnotationHandlerAdapter가 지원하는 handler인지 확인한다.")
	@Test
	void supports_true() {
		// given
		Object handlerExecution = mock(HandlerExecution.class);
		HandlerAdapter handlerAdapter = new AnnotationHandlerAdapter();

		// when
		boolean result = handlerAdapter.supports(handlerExecution);

		// then
		assertThat(result).isTrue();
	}

	@DisplayName("AnnotationHandlerAdapter가 지원하지 않는 handler인지 판별한다.")
	@Test
	void supports_false() {
		// given
		Object controller = mock(Object.class);
		HandlerAdapter handlerAdapter = new AnnotationHandlerAdapter();

		// when
		boolean result = handlerAdapter.supports(controller);

		// then
		assertThat(result).isFalse();
	}

	@DisplayName("handlerException을 처리한다.")
	@Test
	void handle() throws Exception {
		// given
		HandlerExecution handlerExecution = mock(HandlerExecution.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		BDDMockito.given(handlerExecution.handle(request, response))
			.willReturn(new ModelAndView(new JspView("viewName")));

		// when
		HandlerAdapter handlerAdapter = new AnnotationHandlerAdapter();
		ModelAndView modelAndView = handlerAdapter.handle(request, response, handlerExecution);

		// then
		assertAll(
			() -> assertThat(modelAndView.getView()).isInstanceOf(JspView.class),
			() -> assertThat(((JspView)modelAndView.getView()).getViewName()).isEqualTo("viewName"),
			() -> verify(handlerExecution).handle(request, response)
		);

	}
}
