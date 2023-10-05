package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.view.JspView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

class HandlerExecutionTest {
    @Test
    @DisplayName("입력이 무사히 들어오면 핸들러 메서드에 해당 입력을 전달하고 호출한다.")
    void handle() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //given
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        final Method mockHandler = HandlerExecutionTest.class.getDeclaredMethod("mockHandler", HttpServletRequest.class, HttpServletResponse.class);
        final HandlerExecution handlerExecution = new HandlerExecution(mockHandler, mockHandler.getDeclaringClass().newInstance());
        mockHandler.setAccessible(true);

        //when
        final ModelAndView modelAndView = (ModelAndView) handlerExecution.handle(request, response);

        //then
        assertThat(modelAndView.getView())
                .usingRecursiveComparison()
                .isEqualTo(new JspView("test"));
    }

    private ModelAndView mockHandler(final HttpServletRequest request, final HttpServletResponse response) {
        return new ModelAndView(new JspView("test"));
    }
}