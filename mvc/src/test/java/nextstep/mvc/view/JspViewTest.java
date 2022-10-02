package nextstep.mvc.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JspViewTest {

    @DisplayName("redirect를 할 수 있다.")
    @Test
    void redirect() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getRequestURI()).thenReturn("/");
        when(request.getMethod()).thenReturn("GET");

        final JspView jspView = new JspView("redirect:/");

        assertDoesNotThrow(() -> jspView.render(Map.of(), request, response));
    }

    @DisplayName("requestDispatcher를 이용해 forward 해줄 수 있다.")
    @Test
    void forward() throws ServletException, IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher("/index.jsp"))
                .thenReturn(requestDispatcher);

        final JspView jspView = new JspView("/index.jsp");

        assertDoesNotThrow(() -> jspView.render(Map.of(), request, response));
        verify(requestDispatcher).forward(request, response);
    }
}

