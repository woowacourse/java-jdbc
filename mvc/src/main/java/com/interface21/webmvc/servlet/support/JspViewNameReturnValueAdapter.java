package com.interface21.webmvc.servlet.support;

import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.View;
import com.interface21.webmvc.servlet.view.JspView;

public class JspViewNameReturnValueAdapter implements ReturnValueAdapter {

    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String JSP_SUFFIX = ".jsp";

    @Override
    public boolean supports(Object returnValue) {
        return returnValue instanceof String viewName &&
               (viewName.endsWith(JSP_SUFFIX) || viewName.startsWith(REDIRECT_PREFIX));
    }

    @Override
    public ModelAndView adapt(Object returnValue) {
        String viewName = (String) returnValue;
        View view = new JspView(viewName);
        return new ModelAndView(view);
    }
}
