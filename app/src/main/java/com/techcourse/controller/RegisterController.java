package com.techcourse.controller;

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

    private final UserDao userDao = new UserDao(DataSourceConfig.getInstance());

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(final HttpServletRequest request, final HttpServletResponse response) {
        final var user = new User(
                request.getParameter("account"),
                request.getParameter("password"),
                request.getParameter("email"));
        if (userDao.findByAccount(user.getAccount()) != null) {
            return new ModelAndView(new JspView("redirect:/register"));
        }
        userDao.insert(user);

        return new ModelAndView(new JspView("redirect:/index.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(final HttpServletRequest request, final HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }
}
