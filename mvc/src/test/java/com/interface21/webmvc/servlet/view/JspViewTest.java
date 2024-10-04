package com.interface21.webmvc.servlet.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JspViewTest {

    @Test
    @DisplayName("redirect:로 시작하면 redirect 한다.")
    void redirect() throws Exception {
        JspView view = new JspView("redirect:/401.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(null, null, response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/401.html");
    }

    @Test
    @DisplayName("redirect:로 시작하지 않으면 forward 한다.")
    void forward() throws Exception {
        JspView view = new JspView("/401.jsp");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(Map.of(), request, response);

        assertThat(response.getForwardedUrl()).isEqualTo("/401.jsp");
    }

    @Test
    @DisplayName("model이 request attribute에 담긴다.")
    void attribute() throws Exception {
        JspView view = new JspView("/401.jsp");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(Map.of("account", "gugu", "password", "password"), request, response);

        assertAll(
                () -> assertThat(request.getAttribute("account")).isEqualTo("gugu"),
                () -> assertThat(request.getAttribute("account")).isEqualTo("gugu")
        );
    }
}
