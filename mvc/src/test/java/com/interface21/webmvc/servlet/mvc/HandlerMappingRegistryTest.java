package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.webmvc.servlet.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class HandlerMappingRegistryTest {

    private final HandlerMapping dummyHandlerMapping = new HandlerMapping() {
        @Override
        public void initialize() {
        }

        @Override
        public Object getHandler(HttpServletRequest request) {
            return "test";
        }
    };

    @Test
    @DisplayName("핸들러가 존재하는 경우, 해당 핸들러를 반환한다.")
    void getHandler() {
        HandlerMappingRegistry registry = new HandlerMappingRegistry();
        assertThat(registry.getHandler(new MockHttpServletRequest())).isEmpty();

        registry.addHandlerMapping(dummyHandlerMapping);
        Optional<Object> handler = registry.getHandler(new MockHttpServletRequest());
        assertThat(handler).isPresent()
                .get()
                .isEqualTo("test");
    }

    @Test
    @DisplayName("핸들러가 존재하지 않는 경우, 빈 옵셔널을 반환한다.")
    void emptyOptionalOnNoHandlerMapped() {
        HandlerMappingRegistry registry = new HandlerMappingRegistry();
        Optional<Object> handler = registry.getHandler(new MockHttpServletRequest());
        assertThat(handler).isEmpty();
    }
}
