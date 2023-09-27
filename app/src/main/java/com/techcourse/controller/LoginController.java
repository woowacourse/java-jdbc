package com.techcourse.controller;

import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;
import context.org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.org.springframework.web.bind.annotation.GetMapping;
import web.org.springframework.web.bind.annotation.PostMapping;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.View;
import webmvc.org.springframework.web.servlet.view.JspView;

@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/login")
    public ModelAndView getLoginView(final HttpServletRequest req, final HttpServletResponse res) {
        final View view = UserSession.getUserFrom(req.getSession())
                .map(user -> {
                    log.info("logged in {}", user.getAccount());
                    return new JspView("redirect:/index.jsp");
                })
                .orElse(new JspView("/login.jsp"));

        return new ModelAndView(view);
    }

    @PostMapping("/login")
    public ModelAndView login(final HttpServletRequest req, final HttpServletResponse res) {
        if (UserSession.isLoggedIn(req.getSession())) {
            final View view = new JspView("redirect:/index.jsp");
            return new ModelAndView(view);
        }
        final String account = req.getParameter("account");
        final View view = generateView(req, account);
        return new ModelAndView(view);
    }

    private JspView generateView(HttpServletRequest req, String account) {
        return InMemoryUserRepository.findByAccount(account)
                .map(user -> {
                    log.info("User : {}", user);
                    return generateLoginView(req, user);
                })
                .orElse(new JspView("redirect:/401.jsp"));
    }

    private JspView generateLoginView(final HttpServletRequest request, final User user) {
        if (user.checkPassword(request.getParameter("password"))) {
            final var session = request.getSession();
            session.setAttribute(UserSession.SESSION_KEY, user);
            return new JspView("redirect:/index.jsp");
        }
        return new JspView("redirect:/401.jsp");
    }
}
