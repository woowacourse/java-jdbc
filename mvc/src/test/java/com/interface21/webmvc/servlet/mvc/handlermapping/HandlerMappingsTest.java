package com.interface21.webmvc.servlet.mvc.handlermapping;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.bean.container.BeanContainer;
import com.interface21.webmvc.servlet.mvc.HandlerExecution;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import samples.TestFailHandlerMappings;
import samples.TestSuccessHandlerMappings;

class HandlerMappingsTest {

    @BeforeEach
    void setUp() {
        BeanContainer.getInstance().clear();
    }

    @DisplayName("요청을 처리할 수 있는 HandlerMapping 을 찾는다.")
    @Test
    void findHandler() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.registerBeans(List.of(new TestFailHandlerMappings(), new TestSuccessHandlerMappings()));

        HandlerMappings handlerMappings = new HandlerMappings();
        HttpServletRequest request = new MockHttpServletRequest();
        Object handler = handlerMappings.findHandler(request).get();

        assertThat(handler)
                .isInstanceOf(HandlerExecution.class);
    }
}
