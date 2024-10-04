package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.interface21.webmvc.servlet.HandlerExecution;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.View;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HandlerExecutionHandlerAdapterTest {

    @Test
    @DisplayName("HandlerExecution의 결과를 ModelAndView로 변환한다.")
    void convertHandlerExecutionResult() throws Exception {
        HandlerExecution handlerExecution = Mockito.mock(HandlerExecution.class);
        View view = Mockito.mock(View.class);
        ModelAndView modelAndView = new ModelAndView(view);

        when(handlerExecution.getParameters())
                .thenReturn(new Parameter[0]);
        when(handlerExecution.handle(any(Object[].class)))
                .thenReturn(modelAndView);

        HandlerExecutionHandlerAdapter handlerAdapter = new HandlerExecutionHandlerAdapter();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = handlerAdapter.handle(request, response, handlerExecution);

        assertThat(mav).isNotNull()
                .isEqualTo(modelAndView);
    }
}
