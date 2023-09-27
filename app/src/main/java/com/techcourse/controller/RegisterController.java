package com.techcourse.controller;

import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;
import context.org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.org.springframework.web.bind.annotation.GetMapping;
import web.org.springframework.web.bind.annotation.PostMapping;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.View;
import webmvc.org.springframework.web.servlet.view.JspView;

@Controller
public class RegisterController {

    @GetMapping("/register")
    public ModelAndView getRegisterView(final HttpServletRequest req, final HttpServletResponse res) {
        final View view = new JspView("/register.jsp");
        return new ModelAndView(view);
    }

    @PostMapping("/register")
    public ModelAndView register(final HttpServletRequest req, final HttpServletResponse res) {
        final var user = new User(2,
                req.getParameter("account"),
                req.getParameter("password"),
                req.getParameter("email"));
        InMemoryUserRepository.save(user);

        final View view = new JspView("redirect:/index.jsp");
        return new ModelAndView(view);
    }
}
