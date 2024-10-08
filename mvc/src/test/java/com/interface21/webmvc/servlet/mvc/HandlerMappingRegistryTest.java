package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.webmvc.servlet.mvc.tobe.AnnotationHandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HandlerMappingRegistryTest {

    private HandlerMappingRegistry handlerMappingRegistry;
    private AnnotationHandlerMapping annotationHandlerMapping;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handlerMappingRegistry = new HandlerMappingRegistry();
        annotationHandlerMapping = new AnnotationHandlerMapping("samples");
        request = mock(HttpServletRequest.class);
    }

    @Test
    void HandlerMapping을_추가하며_초기화한다() {
        // given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/get-sample");
        assertThat(annotationHandlerMapping.getHandler(request)).isNull();

        // when
        handlerMappingRegistry.addHandlerMapping(annotationHandlerMapping);

        // then
        assertThat(annotationHandlerMapping.getHandler(request)).isNotNull();
    }

    @Test
    void request에_맞는_handler를_반환한다() {
        // given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/get-sample");
        assertFalse(handlerMappingRegistry.findHandler(request).isPresent());

        // when
        handlerMappingRegistry.addHandlerMapping(annotationHandlerMapping);

        // then
        assertTrue(handlerMappingRegistry.findHandler(request).isPresent());
    }

    @Test
    void request에_맞는_handler가_존재하지_않을_땐_빈_옵셔널을_반환한다() {
        // given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/false-path");
        assertFalse(handlerMappingRegistry.findHandler(request).isPresent());

        // when
        handlerMappingRegistry.addHandlerMapping(annotationHandlerMapping);

        // then
        assertFalse(handlerMappingRegistry.findHandler(request).isPresent());
    }
}
