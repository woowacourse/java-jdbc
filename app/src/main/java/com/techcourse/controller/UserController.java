package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.jdbc.core.LegacyJdbcTemplate;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JsonView;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.LegacyUserDaoImpl;
import com.techcourse.dao.LegacyUserHistoryDaoImpl;
import com.techcourse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController() {
        userService = new UserService(
                new LegacyUserDaoImpl(new LegacyJdbcTemplate(DataSourceConfig.getInstance())),
                new LegacyUserHistoryDaoImpl(new LegacyJdbcTemplate(DataSourceConfig.getInstance()))
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
