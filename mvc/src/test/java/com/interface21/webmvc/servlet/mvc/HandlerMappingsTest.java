package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.ContextLoaderTest;
import com.interface21.HandlerContainer;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerMappingsTest {

    static class DummyHandlerMapping implements HandlerMapping {

        private List<String> mappings;

        @Override
        public void initialize() {
            mappings = new ArrayList<>();
            mappings.add("/test");
        }

        @Override
        public Object getHandler(HttpServletRequest request) {
            if (mappings.contains(request.getRequestURI())) {
                return "test success";
            }
            throw new IllegalArgumentException("핸들러 없음");
        }
    }

    @BeforeEach
    void setUp() {
        HandlerContainer instance = HandlerContainer.getInstance();
        instance.clear();
        instance.initialize(ContextLoaderTest.class);
    }

    @DisplayName("HandlerMapping을 구현하는 클래스를 저장한 뒤 적절한 Mapping을 활용해 들어온 요청을 처리한다")
    @Test
    void getHandler() {
        HandlerMappings handlerMappings = new HandlerMappings();
        handlerMappings.initialize();

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURI()).thenReturn("/test");

        assertThat(handlerMappings.getHandler(httpServletRequest)).isEqualTo("test success");
    }

    @DisplayName("일치하는 HandlerMapping이 없을 경우 예외를 발생시킨다")
    @Test
    void notExistMatchHandlerMapping() {
        HandlerMappings handlerMappings = new HandlerMappings();
        handlerMappings.initialize();

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURI()).thenReturn("/notExist");
        when(httpServletRequest.getMethod()).thenReturn(String.valueOf(RequestMethod.POST));

        assertThatThrownBy(() -> handlerMappings.getHandler(httpServletRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
