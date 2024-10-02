package com.interface21.webmvc.servlet.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class HttpServletResponseArgumentResolver implements ArgumentResolver {

    @Override
    public boolean supports(Parameter parameter) {
        Class<?> type = parameter.getType();
        return HttpServletResponse.class.isAssignableFrom(type);
    }

    @Override
    public HttpServletResponse resolveArgument(HttpServletRequest request, HttpServletResponse response,
                                               Parameter parameter) {
        return response;
    }
}
