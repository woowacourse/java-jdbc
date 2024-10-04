package com.interface21.webmvc.servlet.mvc.handleradaptor;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.bean.container.BeanContainer;
import com.interface21.webmvc.servlet.mvc.HandlerAdapter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import samples.TestFailHandlerAdapter;
import samples.TestSuccessHandlerAdapter;

class HandlerAdaptersTest {

    @BeforeEach
    void setUp() {
        BeanContainer.getInstance().clear();
    }

    @DisplayName("Handler 를 처리할 수 있는 HandlerAdaptor 를 찾는다.")
    @Test
    void findHandlerAdaptor() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.registerBeans(List.of(new TestFailHandlerAdapter(), new TestSuccessHandlerAdapter()));

        HandlerAdapters handlerAdapters = new HandlerAdapters();

        HandlerAdapter handlerAdapter = handlerAdapters.findHandlerAdaptor("abc").get();

        assertThat(handlerAdapter)
                .isInstanceOf(TestSuccessHandlerAdapter.class);
    }
}
