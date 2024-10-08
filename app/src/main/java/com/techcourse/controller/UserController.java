package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.web.bind.annotation.RequestParam;
import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public User show(@RequestParam("account") String account) {
        log.debug("user id : {}", account);

        return InMemoryUserRepository.findByAccount(account)
                .orElseThrow();
    }
}
