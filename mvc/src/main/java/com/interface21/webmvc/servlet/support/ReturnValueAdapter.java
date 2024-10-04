package com.interface21.webmvc.servlet.support;

import com.interface21.webmvc.servlet.ModelAndView;

public interface ReturnValueAdapter {

    boolean supports(Object returnValue);

    ModelAndView adapt(Object returnValue);
}
