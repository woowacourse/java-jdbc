package com.interface21.webmvc.servlet.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JspViewTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void 뷰_이름에_redirect가_붙어있으면_리다이렉트한다() throws Exception {
        // given
        String redirectViewName = "redirect:/home";
        JspView jspView = new JspView(redirectViewName);

        // when
        jspView.render(new HashMap<>(), request, response);

        // then
        assertEquals("/home", response.getRedirectedUrl());
    }

    @Test
    void request에_attribute를_추가해_jsp가_값을_받아갈_수_있게한다() throws Exception {
        // given
        String forwardViewName = "/WEB-INF/index.jsp";
        JspView jspView = new JspView(forwardViewName);
        Map<String, Object> model = new HashMap<>();
        model.put("user", "Dora");
        model.put("role", "admin");

        // when
        jspView.render(model, request, response);

        // then
        assertEquals("Dora", request.getAttribute("user"));
        assertEquals("admin", request.getAttribute("role"));
    }
}
