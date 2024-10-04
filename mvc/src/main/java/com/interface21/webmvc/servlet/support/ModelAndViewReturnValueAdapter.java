package com.interface21.webmvc.servlet.support;

import com.interface21.webmvc.servlet.ModelAndView;

public class ModelAndViewReturnValueAdapter implements ReturnValueAdapter {

    @Override
    public boolean supports(Object returnValue) {
        return returnValue instanceof ModelAndView;
    }

    @Override
    public ModelAndView adapt(Object returnValue) {
        return (ModelAndView) returnValue;
    }
}
