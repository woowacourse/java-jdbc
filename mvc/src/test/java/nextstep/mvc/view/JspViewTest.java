package nextstep.mvc.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JspViewTest {

	private HttpServletRequest request;
	private HttpServletResponse response;

	@BeforeEach
	void init() {
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
	}

	@DisplayName("리다익렉트 요청을 처리한다.")
	@Test
	void render_redirect() throws Exception {
		// given
		JspView view = new JspView("redirect:/index.jsp");

		// when
		view.render(Map.of(), request, response);

		// then
		verify(response).sendRedirect("/index.jsp");
	}

	@DisplayName("리다익렉트 이외의 요청을 처리한다.")
	@Test
	void render_forward() throws Exception {
		// given
		RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
		JspView view = new JspView("/index.jsp");

		given(request.getRequestDispatcher("/index.jsp"))
			.willReturn(requestDispatcher);

		// when
		view.render(Map.of(), request, response);

		// then
		assertAll(
			() -> verify(request).getRequestDispatcher("/index.jsp"),
			() -> verify(requestDispatcher).forward(request, response)
		);
	}
}
