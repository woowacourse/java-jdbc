package com.techcourse.controller;

import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.web.bind.annotation.RequestParam;
import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;

@Controller
public class RegisterController {

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String show() {
        return "/register.jsp";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String save(
            @RequestParam("account") String account,
            @RequestParam("password") String password,
            @RequestParam("email") String email
    ) {
        final var user = new User(2, account, password, email);
        InMemoryUserRepository.save(user);

        return "redirect:/index.jsp";
    }
}
