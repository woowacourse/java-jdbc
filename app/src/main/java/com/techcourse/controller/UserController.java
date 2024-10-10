package com.techcourse.controller;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Controller;
import com.interface21.context.stereotype.Inject;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.view.JsonView;
import com.techcourse.domain.User;
import com.techcourse.dto.UserResponse;
import com.techcourse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Inject
    private UserService userService;

    @RequestMapping(value = "/api/user", method = RequestMethod.GET)
    public ModelAndView show(HttpServletRequest request, HttpServletResponse response) {
        String account = request.getParameter("account");
        log.debug("user id : {}", account);

        ModelAndView modelAndView = new ModelAndView(new JsonView());

        User user = userService.findByAccount(account);

        modelAndView.addObject("user", UserResponse.from(user));
        return modelAndView;
    }
}
