package com.techcourse.controller;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final UserDao userDao;

    public LoginController(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
        return UserSession.getUserFrom(request.getSession())
            .map(user -> {
                log.info("logged in {}", user.getAccount());
                return redirect("/index.jsp");
            })
            .orElse(new ModelAndView(new JspView("/login.jsp")));
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
        if (UserSession.isLoggedIn(request.getSession())) {
            return redirect("/index.jsp");
        }
        try {
            final User user = userDao.findByAccount(request.getParameter("acccount"));
            log.info("User : {}", user);
            return login(request, user);
        } catch (RuntimeException e) {
            return redirect("/401.jsp");
        }
    }

    private ModelAndView login(HttpServletRequest request, User user) {
        if (user.checkPassword(request.getParameter("password"))) {
            final HttpSession session = request.getSession();
            session.setAttribute(UserSession.SESSION_KEY, user);
            return redirect("/index.jsp");
        } else {
            return redirect("/401.jsp");
        }
    }

    private ModelAndView redirect(String path) {
        return new ModelAndView(new JspView(JspView.REDIRECT_PREFIX + path));
    }
}
