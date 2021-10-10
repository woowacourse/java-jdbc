package com.techcourse.controller;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;

@Controller
public class RegisterController {

    private final UserDao userDao;

    @Autowired
    public RegisterController(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(HttpServletRequest request, HttpServletResponse response) {

        Optional<User> sameAccountUser = userDao.findByAccount(request.getParameter("account"));
        if (sameAccountUser.isPresent()) {
            response.setStatus(409);
            return new ModelAndView(new JspView("/409.jsp"));
        }

        User insertedUser = userDao.insert(new User(
            request.getParameter("account"),
            request.getParameter("password"),
            request.getParameter("email"))
        );
        response.setStatus(201);
        response.setHeader("Location", "/api/user?account=" + insertedUser.getAccount());
        return new ModelAndView(new JspView("/index.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }
}
