package com.interface21.webmvc.servlet.mvc.returnvaluehandler;

import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.ReturnValueHandler;
import com.interface21.webmvc.servlet.view.JsonView;

public class JsonReturnValueHandler implements ReturnValueHandler {

    private static final String DEFAULT_ATTRIBUTE_NAME = "value";

    @Override
    public boolean supports(Object returnValue) {
        return !(returnValue instanceof ModelAndView || returnValue instanceof String);
    }

    @Override
    public ModelAndView handle(Object returnValue) {
        ModelAndView modelAndView = new ModelAndView(new JsonView());
        modelAndView.addObject(DEFAULT_ATTRIBUTE_NAME, returnValue);
        return modelAndView;
    }
}
