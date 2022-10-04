package com.techcourse.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;

import jakarta.servlet.http.HttpServletRequest;
import nextstep.mvc.view.JsonView;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.annotation.RequestParam;
import nextstep.web.support.RequestMethod;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public ModelAndView findUser(@RequestParam(name = "account") String account) {
        log.debug("user id : {}", account);

        try {
            return response(account);
        } catch (NoSuchElementException e) {
            return new ModelAndView(new JspView("404.jsp"));
        }
    }

    private ModelAndView response(String account) {
        ModelAndView modelAndView = new ModelAndView(new JsonView());
        if (account != null) {
            return responseUser(account, modelAndView);
        }
        return responseAllUsers(modelAndView);
    }

    private ModelAndView responseUser(String account, ModelAndView modelAndView) {
        final User user = InMemoryUserRepository.findByAccount(account)
            .orElseThrow();
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    private ModelAndView responseAllUsers(ModelAndView modelAndView) {
        List<User> users = InMemoryUserRepository.findAll();
        modelAndView.addObject("users", users);
        return modelAndView;
    }
}
