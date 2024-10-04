package com.interface21.webmvc.servlet.mvc.argumentresolver;

import com.interface21.webmvc.servlet.mvc.ArgumentResolver;
import com.interface21.webmvc.servlet.mvc.MethodParameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DefaultRequestArgumentResolver implements ArgumentResolver {

    @Override
    public boolean supports(MethodParameter methodParameter) {
        return methodParameter.isType(HttpServletRequest.class);
    }

    @Override
    public Object resolveArgument(HttpServletRequest request, HttpServletResponse response, MethodParameter methodParameter) {
        return request;
    }
}
