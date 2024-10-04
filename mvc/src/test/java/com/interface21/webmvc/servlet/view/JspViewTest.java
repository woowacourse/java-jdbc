package com.interface21.webmvc.servlet.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JspViewTest {

    @Test
    @DisplayName("redirect:로 시작하는 경우, Response를 redirect로 설정한다.")
    void redirectResponse() throws Exception {
        JspView view = new JspView("redirect:/1234");
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(null, null, response);
        assertEquals("/1234", response.getRedirectedUrl());
    }

    @Test
    @DisplayName("model의 attribute를 request에 설정한다.")
    void setRequestAttributeFromModel() throws Exception {
        JspView view = new JspView("/index.jsp");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> model = Map.of("name", "테니");

        view.render(model, request, response);
        Object actual = request.getAttribute("name");
        assertThat(actual).isEqualTo("테니");
    }

    @Test
    @DisplayName("redirect:로 시작하지 않는 경우, RequestDispatcher에게 위임한다.")
    void forwardRequest() throws Exception {
        JspView view = new JspView("/index.jsp");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        view.render(Collections.emptyMap(), request, response);
        assertThat(response.getForwardedUrl()).isEqualTo("/index.jsp");
    }
}
