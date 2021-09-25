package com.techcourse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.ModelAndView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ForwardController는")
class ForwardControllerTest {

    private HttpServletRequest request;
    private HttpServletResponse response;

    private ForwardController forwardController;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        forwardController = new ForwardController();
    }

    @DisplayName("/ 경로 요청시 index.jsp 화면을 반환한다.")
    @Test
    void show() {
        // given
        when(request.getRequestURI()).thenReturn("/");
        when(request.getMethod()).thenReturn("GET");

        // when
        ModelAndView modelAndView = forwardController.page(request, response);

        // then
        assertThat(modelAndView.getViewName()).isEqualTo("/index.jsp");
    }
}