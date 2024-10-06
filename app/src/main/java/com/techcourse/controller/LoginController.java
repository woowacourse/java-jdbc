package com.techcourse.controller;

import javax.sql.DataSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JspView;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.service.UserService;

@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final UserService userService;

    public LoginController() {
        DataSource instance = DataSourceConfig.getInstance();
        UserDao userDao = new UserDao(instance);
        UserHistoryDao userHistoryDao = new UserHistoryDao(instance);
        this.userService = new UserService(userDao, userHistoryDao);
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView view(final HttpServletRequest request, final HttpServletResponse response) {
        return UserSession.getUserFrom(request.getSession())
                .map(user -> {
                    log.info("logged in {}", user.getAccount());
                    return redirect("/index.jsp");
                })
                .orElse(new ModelAndView(new JspView("/login.jsp")));
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(final HttpServletRequest request, final HttpServletResponse response) {
        if (UserSession.isLoggedIn(request.getSession())) {
            return redirect("/index.jsp");
        }
        try {
            User user = userService.findByAccount(request.getParameter("account"));
            log.info("User : {}", user);
            return login(request, user);
        } catch (IllegalArgumentException e) {
            return redirect("/401.jsp");
        }
    }

    private ModelAndView login(final HttpServletRequest request, final User user) {
        if (user.checkPassword(request.getParameter("password"))) {
            final var session = request.getSession();
            session.setAttribute(UserSession.SESSION_KEY, user);
            return redirect("/index.jsp");
        } else {
            return redirect("/401.jsp");
        }
    }

    private ModelAndView redirect(final String path) {
        return new ModelAndView(new JspView(JspView.REDIRECT_PREFIX + path));
    }
}
