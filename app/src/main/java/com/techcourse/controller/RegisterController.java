package com.techcourse.controller;

import com.interface21.dao.DuplicateKeyException;
import com.techcourse.domain.User;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
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
        this(new UserDao(DataSourceConfig.getInstance()));
    }

    public RegisterController(final UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(final HttpServletRequest request, final HttpServletResponse response) {
        final User user;
        try {
            user = new User(
                    request.getParameter("account"),
                    request.getParameter("password"),
                    request.getParameter("email"));
        } catch (IllegalArgumentException e) {
            return new ModelAndView(new JspView("redirect:/register"));
        }
        if (userDao.findByAccount(user.getAccount()) != null) {
            return new ModelAndView(new JspView("redirect:/register"));
        }
        try {
            userDao.insert(user);
        } catch (DuplicateKeyException e) {
            return new ModelAndView(new JspView("redirect:/register"));
        }

        return new ModelAndView(new JspView("redirect:/index.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(final HttpServletRequest request, final HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }
}
