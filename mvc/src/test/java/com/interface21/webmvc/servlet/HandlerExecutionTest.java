package com.interface21.webmvc.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.interface21.core.BeanContainer;
import com.interface21.core.BeanContainerFactory;
import com.interface21.core.FakeBeanContainer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerExecutionTest {

    static class DummyController {

        public DummyController() {
        }

        public ModelAndView test(HttpServletRequest request, HttpServletResponse response) {
            ModelAndView mav = mock(ModelAndView.class);
            when(mav.getObject("test")).thenReturn("test");
            return mav;
        }
    }

    @Test
    @DisplayName("핸들러의 메서드를 실행한다.")
    void executeHandler() throws Exception {
        var factory = mockStatic(BeanContainerFactory.class);
        BeanContainer container = new FakeBeanContainer(new HashMap<>());
        factory.when(BeanContainerFactory::getContainer)
                .thenReturn(container);

        container.registerBean(DummyController.class);
        Method method = DummyController.class.getDeclaredMethod(
                "test", HttpServletRequest.class, HttpServletResponse.class
        );
        HandlerExecution handlerExecution = new HandlerExecution(method);
        ModelAndView actual = (ModelAndView) handlerExecution.handle(null, null);
        assertThat(actual.getObject("test")).isEqualTo("test");
        factory.close();
    }
}
