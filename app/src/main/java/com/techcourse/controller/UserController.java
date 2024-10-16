package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JsonView;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TxUserService;
import com.techcourse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    private final UserDao userDao = new UserDao(jdbcTemplate);
    private final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
    private final UserService userService = new TxUserService(DataSourceConfig.getInstance(),
            new AppUserService(userDao, userHistoryDao));

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public ModelAndView show(final HttpServletRequest request, final HttpServletResponse response) {
        String rawId = request.getParameter("id");
        log.debug("user id : {}", rawId);

        userService.save(new User("mia", "password", "mia@gmaill.com"));

        ModelAndView modelAndView = new ModelAndView(new JsonView());
        Long id = Long.valueOf(rawId);
        User user = userService.findById(id);

        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
