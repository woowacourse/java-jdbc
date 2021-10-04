package com.techcourse.controller;

import com.techcourse.controller.request.LoginRequest;
import com.techcourse.domain.User;
import com.techcourse.exception.UnauthorizedException;
import com.techcourse.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView show(HttpServletRequest request, HttpServletResponse response) {
        if (UserSession.isAlreadyLogin(request.getSession())) {
            User user = UserSession.getUser(request.getSession());
            LOG.debug("logged in {}", user.getAccount());
            return new ModelAndView(new JspView("redirect:/index.jsp"));
        }

        return new ModelAndView(new JspView("/login.jsp"));
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
        if (UserSession.isAlreadyLogin(request.getSession())) {
            return new ModelAndView(new JspView("redirect:/index.jsp"));
        }

        try {
            LoginRequest loginRequest = getLoginRequest(request);
            loginService.login(loginRequest);

            LOG.debug("Login Success!!");

            return new ModelAndView(new JspView("redirect:/index.jsp"));
        } catch (UnauthorizedException e) {
            LOG.debug("Login Failed...");

            return new ModelAndView(new JspView("redirect:/401.jsp"));
        }
    }

    private LoginRequest getLoginRequest(HttpServletRequest request) {
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        HttpSession httpSession = request.getSession();

        LOG.debug("Login Request => account: {}", account);

        return new LoginRequest(account, password, httpSession);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
        final HttpSession session = request.getSession();
        session.removeAttribute(UserSession.SESSION_KEY);

        return new ModelAndView(new JspView("redirect:/"));
    }
}
