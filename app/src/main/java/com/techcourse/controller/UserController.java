package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JsonView;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDaoImpl;
import com.techcourse.dao.UserHistoryDaoImpl;
import com.techcourse.service.UserService;
import com.techcourse.service.AppUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController() {
        userService = new AppUserService(
                new UserDaoImpl(DataSourceConfig.getInstance()),
                new UserHistoryDaoImpl(DataSourceConfig.getInstance())
        );
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public ModelAndView show(final HttpServletRequest request, final HttpServletResponse response) {
        final var account = request.getParameter("account");
        log.debug("user id : {}", account);

        final var modelAndView = new ModelAndView(new JsonView());
        final var user = userService.findByAccount(account);

        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
