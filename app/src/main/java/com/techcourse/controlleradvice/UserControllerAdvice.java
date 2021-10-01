package com.techcourse.controlleradvice;

import com.techcourse.exception.LoginFailException;
import com.techcourse.exception.RegisterFailException;
import nextstep.mvc.Pages;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.ControllerAdvice;
import nextstep.web.annotation.ExceptionHandler;

@ControllerAdvice
public class UserControllerAdvice {

    @ExceptionHandler(value = LoginFailException.class)
    public ModelAndView handleLoginFailException(LoginFailException e) {
        return new ModelAndView(new JspView(Pages.UNAUTHORIZED.redirectPageName()));
    }

    @ExceptionHandler(value = RegisterFailException.class)
    public ModelAndView handleRegisterFailException(RegisterFailException e) {
        return new ModelAndView(new JspView(Pages.UNAUTHORIZED.redirectPageName()));
    }
}
