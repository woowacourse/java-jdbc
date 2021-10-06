package com.techcourse.controller;

import com.techcourse.exception.UserNotFoundException;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.JspView;
import nextstep.web.annotation.Autowired;
import nextstep.mvc.view.JsonView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserDao userDao;

    @Autowired
    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public ModelAndView show(HttpServletRequest request, HttpServletResponse response) {
        String account = request.getParameter("account");
        log.debug("user id : {}", account);

        try {
            User user = userDao.findByAccount(account)
                .orElseThrow(() -> new UserNotFoundException(account));

            ModelAndView modelAndView = new ModelAndView(new JsonView());
            modelAndView.addObject("user", user);
            return modelAndView;
        } catch (UserNotFoundException exception) {
            return new ModelAndView(new JspView("/404.jsp"));
        }
    }
}
