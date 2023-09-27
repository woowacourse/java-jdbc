package com.techcourse.controller;

import context.org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.org.springframework.web.bind.annotation.GetMapping;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.View;
import webmvc.org.springframework.web.servlet.view.JspView;

@Controller
public class ForwardController {

    @GetMapping("/")
    public ModelAndView getIndexView(HttpServletRequest req, HttpServletResponse res) {
        View view = new JspView("index.jsp");
        return new ModelAndView(view);
    }
}
