package com.interface21.webmvc.servlet.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class HttpServletRequestArgumentResolver implements ArgumentResolver {

    @Override
    public boolean supports(Parameter parameter) {
        Class<?> type = parameter.getType();
        return HttpServletRequest.class.isAssignableFrom(type);
    }

    @Override
    public HttpServletRequest resolveArgument(HttpServletRequest request, HttpServletResponse response,
                                              Parameter parameter) {
        return request;
    }
}
