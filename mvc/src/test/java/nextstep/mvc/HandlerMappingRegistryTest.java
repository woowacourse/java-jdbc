package nextstep.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import nextstep.mvc.controller.AnnotationHandlerMapping;
import nextstep.mvc.controller.HandlerExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerMappingRegistryTest {

    @DisplayName("HandlerMapping 인터페이스를 구현한 인스턴스를 추가하고 핸들러를 찾을 수 있다.")
    @Test
    void addHandlerMappingAndGetHandler() {
        // given
        final HandlerMappingRegistry handlerMappingRegistry = new HandlerMappingRegistry();
        final AnnotationHandlerMapping annotationHandlerMapping = new AnnotationHandlerMapping("samples");
        annotationHandlerMapping.initialize();

        // when
        handlerMappingRegistry.addHandlerMapping(annotationHandlerMapping);

        final var request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        // then
        final Object handler = handlerMappingRegistry.getHandler(request).orElseThrow();
        assertThat(handler).isInstanceOf(HandlerExecution.class);
    }

    @DisplayName("핸들러 매핑을 찾을 수 없는 경우 Optional Empty 를 반환한다.")
    @Test
    void notFoundHandlerMapping() {
        /// given
        final HandlerMappingRegistry handlerMappingRegistry = new HandlerMappingRegistry();
        final AnnotationHandlerMapping annotationHandlerMapping = new AnnotationHandlerMapping();
        annotationHandlerMapping.initialize();

        // when
        handlerMappingRegistry.addHandlerMapping(annotationHandlerMapping);

        final var request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        // then
        assertThat(handlerMappingRegistry.getHandler(request)).isEmpty();
    }
}
