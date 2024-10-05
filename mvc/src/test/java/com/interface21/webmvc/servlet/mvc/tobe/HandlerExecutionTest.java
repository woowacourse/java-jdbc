package com.interface21.webmvc.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.webmvc.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.webmvc.servlet.mvc.sample.TestController;

class HandlerExecutionTest {

    static class NotExistDefaultConstructorController {

        public NotExistDefaultConstructorController(String sample) {}

        public ModelAndView test(HttpServletRequest request, HttpServletResponse response) {
            return null;
        }
    }

    @DisplayName("인자로 넘겨준 메소드를 실행한다")
    @Test
    void handle() throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(
                "test",
                HttpServletRequest.class,
                HttpServletResponse.class
        );
        HandlerExecution handlerExecution = new HandlerExecution(method);
        ModelAndView modelAndView = handlerExecution.handle(null, null);

        assertThat(modelAndView.getObject("test")).isEqualTo("test");
    }

    @DisplayName("기본 생성자가 없을 경우 예외를 발생시킨다")
    @Test
    void notExistDefaultConstructor() throws NoSuchMethodException {
        Method method = NotExistDefaultConstructorController.class.getDeclaredMethod(
                "test",
                HttpServletRequest.class,
                HttpServletResponse.class
        );

        assertThatThrownBy(() -> new HandlerExecution(method))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기본 생성자가 존재하지 않습니다");
    }
}
