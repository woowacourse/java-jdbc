package com.techcourse.controller;

import com.techcourse.controller.request.RegisterRequest;
import com.techcourse.exception.DuplicateAccountException;
import com.techcourse.service.RegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class RegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView show(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView(new JspView("/register.jsp"));
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {
        try {
            RegisterRequest registerRequest = getRegisterRequest(request);
            registerService.registerUser(registerRequest);

            LOG.debug("Register Success!!");

            return new ModelAndView(new JspView("redirect:/index.jsp"));
        } catch (DuplicateAccountException e) {
            LOG.debug("Register Failed...");

            return new ModelAndView(new JspView("redirect:/409.jsp"));
        }
    }

    private RegisterRequest getRegisterRequest(HttpServletRequest request) {
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        LOG.debug("Register Request => account: {}, email: {}", account, email);

        return new RegisterRequest(account, password, email);
    }
}
