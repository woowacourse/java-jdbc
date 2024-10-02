package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.webmvc.servlet.HandlerAdapter;
import com.interface21.webmvc.servlet.ModelAndView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HandlerAdapterRegistryTest {

    @Test
    @DisplayName("적합한 핸들러 어댑터를 찾지 못하는 경우, 예외를 발생한다.")
    void handlerAdapterNotFound() {
        HandlerAdapterRegistry registry = new HandlerAdapterRegistry();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> registry.handle(request, response, new String[0]))
                .isInstanceOf(ServletException.class);
    }

    @Test
    @DisplayName("핸들러 어댑터를 찾는 경우, 해당 어댑터에 실행을 위임한다.")
    void handleRequest() throws Exception {
        HandlerAdapterRegistry registry = new HandlerAdapterRegistry();
        HandlerAdapter adapter = mock(HandlerAdapter.class);
        ModelAndView mav = mock(ModelAndView.class);
        when(adapter.supports(any())).thenReturn(true);
        when(adapter.handle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                .thenReturn(mav);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        registry.addHandlerAdapter(adapter);
        assertDoesNotThrow(() -> registry.handle(request, response, new String[0]));
    }
}
