package com.techcourse.controller;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.DuplicateAccountException;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JspView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class RegisterController {

    private final UserDao userDao;

    public RegisterController() {
        this(new UserDao(DataSourceConfig.getInstance()));
    }

    RegisterController(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(
            final HttpServletRequest request,
            final HttpServletResponse response) {

        final var user = new User(
                request.getParameter("account"),
                request.getParameter("password"),
                request.getParameter("email")
        );

        try {
            userDao.insert(user);
        } catch (DuplicateAccountException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);

            return new ModelAndView(new JspView("/register.jsp"))
                    .addObject("errorMessage", "이미 사용 중인 아이디입니다.");
        }

        return new ModelAndView(new JspView("redirect:/index.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(
            final HttpServletRequest request,
            final HttpServletResponse response) {

        return new ModelAndView(new JspView("/register.jsp"));
    }
}
