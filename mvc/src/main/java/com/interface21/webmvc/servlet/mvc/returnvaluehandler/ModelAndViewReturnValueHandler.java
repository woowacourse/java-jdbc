package com.interface21.webmvc.servlet.mvc.returnvaluehandler;

import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.ReturnValueHandler;

public class ModelAndViewReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean supports(Object returnValue) {
        return returnValue instanceof ModelAndView;
    }

    @Override
    public ModelAndView handle(Object returnValue) {
        return (ModelAndView) returnValue;
    }
}
