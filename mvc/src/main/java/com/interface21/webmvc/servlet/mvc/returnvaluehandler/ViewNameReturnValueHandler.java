package com.interface21.webmvc.servlet.mvc.returnvaluehandler;

import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.ReturnValueHandler;
import com.interface21.webmvc.servlet.view.JspView;

public class ViewNameReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean supports(Object returnValue) {
        return returnValue instanceof String;
    }

    @Override
    public ModelAndView handle(Object returnValue) {
        String viewName = (String) returnValue;
        return new ModelAndView(new JspView(viewName));
    }
}
