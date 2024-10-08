package com.interface21.webmvc.servlet.mvc.argumentresolver;

import com.interface21.webmvc.servlet.mvc.ArgumentResolver;
import com.interface21.webmvc.servlet.mvc.MethodParameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DefaultResponseArgumentResolver implements ArgumentResolver {

    @Override
    public boolean supports(MethodParameter methodParameter) {
        return methodParameter.isType(HttpServletResponse.class);
    }

    @Override
    public Object resolveArgument(HttpServletRequest request, HttpServletResponse response, MethodParameter methodParameter) {
        return response;
    }
}
