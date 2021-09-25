package nextstep.mvc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Set;
import nextstep.mvc.controller.HandlerExecution;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Controller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

@DisplayName("HandlerExecution은")
class HandlerExecutionTest {

    @DisplayName("handle 호출시 생성자로 받은 instance의 method 동작을 수행한다.")
    @Test
    void handle() throws Exception {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getAttribute("id")).thenReturn("gugu");
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        Class<?> handler = findHandler();

        Object instance = handler.getConstructor().newInstance();
        Method method = handler.getMethod("findUserId", HttpServletRequest.class, HttpServletResponse.class);

        // when
        HandlerExecution handlerExecution = new HandlerExecution(instance, method);
        ModelAndView modelAndView = handlerExecution.handle(request, response);

        // then
        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }

    private Class<?> findHandler() {
        Reflections reflections = new Reflections("samples");
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);

        return controllers.stream()
            .findAny()
            .orElseThrow();
    }
}
