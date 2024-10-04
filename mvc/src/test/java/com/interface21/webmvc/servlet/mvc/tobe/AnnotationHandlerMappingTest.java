package com.interface21.webmvc.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.bean.BeanRegister;
import com.interface21.bean.container.BeanContainer;
import com.interface21.bean.scanner.ComponentScanner;
import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.HandlerExecution;
import com.interface21.webmvc.servlet.mvc.handlermapping.AnnotationHandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnnotationHandlerMappingTest {

    private AnnotationHandlerMapping handlerMapping;

    @BeforeEach
    void setUp() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.clear();
        beanContainer.registerBeans(List.of(new samples.TestController()));
        handlerMapping = new AnnotationHandlerMapping();
        handlerMapping.initialize();
    }

    @Test
    void get() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        when(request.getAttribute("id")).thenReturn("gugu");
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        final var handlerExecution = (HandlerExecution) handlerMapping.getHandler(request);
        final var modelAndView = (ModelAndView) handlerExecution.handle(request, response);

        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }

    @Test
    void post() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        when(request.getAttribute("id")).thenReturn("gugu");
        when(request.getRequestURI()).thenReturn("/post-test");
        when(request.getMethod()).thenReturn("POST");

        final var handlerExecution = (HandlerExecution) handlerMapping.getHandler(request);
        final var modelAndView = (ModelAndView) handlerExecution.handle(request, response);

        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }

    @DisplayName("중복된 url과 method로 handlerMapping에 추가 할 수 없다.")
    @Test
    void initialize() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        List<Object> beans = ComponentScanner.componentScan("com.interface21.webmvc.servlet.mvc.tobe").stream()
                .map(BeanRegister::createBean)
                .toList();
        beanContainer.registerBeans(beans);
        handlerMapping = new AnnotationHandlerMapping();
        assertThatThrownBy(() -> handlerMapping.initialize())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Component
    @Controller
    public static class TestController1 {

        @RequestMapping(value = "/test", method = RequestMethod.GET)
        public ModelAndView test1(final HttpServletRequest request, final HttpServletResponse response) {
            return null;
        }

        @RequestMapping(value = "/test", method = RequestMethod.GET)
        public ModelAndView findUserId2(final HttpServletRequest request, final HttpServletResponse response) {
            return null;
        }
    }

    @DisplayName("Handler를 찾을 수 없으면 예외가 발생한다.")
    @Test
    void validateHandlerKey() {
        final var request = mock(HttpServletRequest.class);

        when(request.getAttribute("id")).thenReturn("gugu");
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("DELETE");

        assertThatThrownBy(() -> handlerMapping.getHandler(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 요청입니다.");
    }
}
