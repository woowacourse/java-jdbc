package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JspView;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TransactionUserService;
import com.techcourse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController() {
        userService = new TransactionUserService(
                new AppUserService(
                        new UserDao(new JdbcTemplate(DataSourceConfig.getInstance())),
                        new UserHistoryDao(new JdbcTemplate(DataSourceConfig.getInstance()))
                ));
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(final HttpServletRequest request, final HttpServletResponse response) {
        final var user = new User(2,
                request.getParameter("account"),
                request.getParameter("password"),
                request.getParameter("email"));
        userService.save(user);

        return new ModelAndView(new JspView("redirect:/index.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(final HttpServletRequest request, final HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }
}
