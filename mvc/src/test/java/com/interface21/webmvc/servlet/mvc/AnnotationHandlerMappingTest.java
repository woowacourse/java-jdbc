package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.interface21.context.stereotype.Controller;
import com.interface21.core.BeanContainer;
import com.interface21.core.BeanContainerFactory;
import com.interface21.core.BeanRegistrar;
import com.interface21.core.FakeBeanContainer;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.HandlerExecution;
import com.interface21.webmvc.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import samples.TestController;

class AnnotationHandlerMappingTest {

    private AnnotationHandlerMapping handlerMapping;

    MockedStatic<BeanContainerFactory> mockFactory;

    @BeforeEach
    void setUp() {
        BeanContainer container = new FakeBeanContainer(new HashMap<>());
        mockFactory = mockStatic(BeanContainerFactory.class);
        mockFactory.when(BeanContainerFactory::getContainer)
                .thenReturn(container);
        BeanRegistrar.registerBeans(getClass());
        BeanRegistrar.registerBeans(TestController.class);

        handlerMapping = new AnnotationHandlerMapping();
        handlerMapping.initialize();
    }

    @AfterEach
    void tearDown() {
        mockFactory.close();
    }

    @Test
    void get() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getAttribute("id")).thenReturn("gugu");
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        HandlerExecution handlerExecution = handlerMapping.getHandler(request);
        ModelAndView modelAndView = (ModelAndView) handlerExecution.handle(request, response);

        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }

    @Test
    void post() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getAttribute("id")).thenReturn("gugu");
        when(request.getRequestURI()).thenReturn("/post-test");
        when(request.getMethod()).thenReturn("POST");

        HandlerExecution handlerExecution = handlerMapping.getHandler(request);
        ModelAndView modelAndView = (ModelAndView) handlerExecution.handle(request, response);

        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }

    @Controller
    static class DummyController {
        public DummyController() {}

        @RequestMapping(value = "/test", method = RequestMethod.GET)
        public ModelAndView test(HttpServletRequest request, HttpServletResponse response) {
            return null;
        }

        @RequestMapping(value = "/noRequesetMappingValueSet")
        public ModelAndView noRequestMappingValueSet(HttpServletRequest request, HttpServletResponse response) {
            return null;
        }
    }

    @Test
    @DisplayName("Controller 어노테이션을 찾아, RequestMapping이 존재하는 메서드를 핸들러로 등록한다.")
    void registerHandler() {
        AnnotationHandlerMapping annotationHandlerMapping = new AnnotationHandlerMapping();
        annotationHandlerMapping.initialize();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");

        Object handler = annotationHandlerMapping.getHandler(request);
        assertThat(handler).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(RequestMethod.class)
    @DisplayName("RequestMapping 어노테이션의 value가 비어있는 경우, 모든 메서드를 등록한다")
    void registerHandlerWithNoRequestMappingValue(RequestMethod method) {
        AnnotationHandlerMapping annotationHandlerMapping = new AnnotationHandlerMapping();
        annotationHandlerMapping.initialize();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/noRequesetMappingValueSet");
        when(request.getMethod()).thenReturn(method.name());

        Object handler = annotationHandlerMapping.getHandler(request);
        assertThat(handler).isNotNull();
    }
}
