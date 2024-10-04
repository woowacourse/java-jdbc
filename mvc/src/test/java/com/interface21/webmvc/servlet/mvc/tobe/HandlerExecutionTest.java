package com.interface21.webmvc.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.bean.container.BeanContainer;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.HandlerExecution;
import com.interface21.webmvc.servlet.view.JspView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HandlerExecutionTest {

    @BeforeEach
    void setUp() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.clear();
        beanContainer.registerBeans(List.of(new TestHandlerExecution()));
    }

    @DisplayName("Method를 handle로 실행시킨다.")
    @Test
    void handle() throws Exception {
        Method method = TestHandlerExecution.class.getMethod(
                "test", HttpServletRequest.class, HttpServletResponse.class);
        HandlerExecution handlerExecution = new HandlerExecution(method);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ModelAndView modelAndView = (ModelAndView) handlerExecution.handle(request, response);

        assertThat(modelAndView.getObject("gugu")).isEqualTo("haha");
    }

    public static class TestHandlerExecution {

        private static ModelAndView createModelAndView() {
            ModelAndView modelAndView = new ModelAndView(new JspView("abc"));
            modelAndView.addObject("gugu", "haha");
            return modelAndView;
        }

        public ModelAndView test(HttpServletRequest request, HttpServletResponse response) {
            return createModelAndView();
        }

        public ModelAndView testSwitch(HttpServletResponse response, HttpServletRequest request) {
            return createModelAndView();
        }

        public ModelAndView onlyRequest(HttpServletRequest request) {
            return createModelAndView();
        }

        public ModelAndView onlyResponse(HttpServletResponse response) {
            return createModelAndView();
        }

        public ModelAndView none() {
            return createModelAndView();
        }

        public ModelAndView notSupport(Long id) {
            return createModelAndView();
        }
    }
}
