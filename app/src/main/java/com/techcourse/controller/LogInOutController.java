package com.techcourse.controller;

import com.techcourse.domain.UserSession;
import com.techcourse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nextstep.mvc.Pages;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class LogInOutController {

    private static final Logger LOG = LoggerFactory.getLogger(LogInOutController.class);

    private final UserService userService;

    @Autowired
    public LogInOutController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView getLoginPage(HttpServletRequest request, HttpServletResponse response) {
        String viewName = UserSession.getUserFrom(request.getSession())
            .map(user -> {
                LOG.info("logged in {}", user.getAccount());
                return Pages.INDEX.redirectPageName();
            })
            .orElse(Pages.LOGIN.getPageName());
        return new ModelAndView(new JspView(viewName));
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
        if (UserSession.isLoggedIn(request.getSession())) {
            return new ModelAndView(new JspView(Pages.INDEX.redirectPageName()));
        }
        Pages pages = userService.checkedLogin(request);
        return new ModelAndView(new JspView(pages.redirectPageName()));
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
        final HttpSession session = request.getSession();
        session.removeAttribute(UserSession.SESSION_KEY);
        return new ModelAndView(new JspView(Pages.INDEX.redirectPageName()));
    }
}
