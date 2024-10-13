package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.transaction.support.TransactionManager;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JsonView;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TxUserService;
import com.techcourse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController() {
        DataSource dataSource = DataSourceConfig.getInstance();
        UserDao userDao = new UserDao(dataSource);
        UserHistoryDao userHistoryDao = new UserHistoryDao(dataSource);
        UserService appUserService = new AppUserService(userDao, userHistoryDao);
        TransactionManager transactionManager = new TransactionManager(dataSource);
        this.userService = new TxUserService(appUserService, transactionManager);
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public ModelAndView show(final HttpServletRequest request, final HttpServletResponse response) {
        final var account = request.getParameter("account");
        log.debug("user id : {}", account);

        final var modelAndView = new ModelAndView(new JsonView());
        final var user = Optional.ofNullable(userService.findByAccount(account))
                .orElseThrow();

        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
