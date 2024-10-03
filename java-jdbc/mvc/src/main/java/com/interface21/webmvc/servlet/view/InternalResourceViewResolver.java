package com.interface21.webmvc.servlet.view;

public class InternalResourceViewResolver implements ViewResolver {

    private static final String DEFAULT_EXTENSION = ".jsp";

    @Override
    public View resolveViewName(String viewName) {
        return new JspView(viewName + DEFAULT_EXTENSION);
    }
}
