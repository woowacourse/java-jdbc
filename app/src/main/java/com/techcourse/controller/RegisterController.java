package com.techcourse.controller;

import com.techcourse.config.AppConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.interface21.webmvc.servlet.view.JspView;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;

@Controller
public class RegisterController {

    private final UserDao userDao;

    public RegisterController() {
        this(AppConfig.getInstance().getUserDao());
    }

    public RegisterController(final UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(final HttpServletRequest request, final HttpServletResponse response) {
        final var user = new User(
                request.getParameter("account"),
                request.getParameter("password"),
                request.getParameter("email"));
        userDao.insert(user);

        return new ModelAndView(new JspView("redirect:/index.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(final HttpServletRequest request, final HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }
}
