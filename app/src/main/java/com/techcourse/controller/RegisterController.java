package com.techcourse.controller;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;

@Controller
public class RegisterController {

    private final UserDao userDao = new UserDao(DataSourceConfig.getInstance());

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(HttpServletRequest request, HttpServletResponse response) {
        final String account = request.getParameter("account");
        final User user = new User(2,
                account,
                request.getParameter("password"),
                request.getParameter("email"));
        if (userDao.findByAccount(account).isPresent()) {
            return new ModelAndView(new JspView("redirect:/500.jsp"));
        }
        userDao.insert(user);
        return new ModelAndView(new JspView("redirect:/"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }
}
