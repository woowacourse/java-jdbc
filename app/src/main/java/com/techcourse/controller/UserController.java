package com.techcourse.controller;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserDao userDao;

    public UserController() {
        this(new UserDao(DataSourceConfig.getInstance()));
    }

    UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public ModelAndView show(
            final HttpServletRequest request,
            final HttpServletResponse response) {

        final var account = request.getParameter("account");
        log.debug("user account: {}", account);

        final var user = userDao.findByAccount(account);

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(new JsonView());
        }

        final var modelAndView = new ModelAndView(new JsonView());
        modelAndView.addObject("user", Map.of(
                "id", user.getId(),
                "account", user.getAccount(),
                "email", user.getEmail()
        ));

        return modelAndView;
    }
}
