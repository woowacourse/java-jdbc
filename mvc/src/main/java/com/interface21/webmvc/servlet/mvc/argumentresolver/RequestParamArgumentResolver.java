package com.interface21.webmvc.servlet.mvc.argumentresolver;

import com.interface21.core.util.StringConverter;
import com.interface21.web.bind.annotation.RequestParam;
import com.interface21.webmvc.servlet.mvc.ArgumentResolver;
import com.interface21.webmvc.servlet.mvc.MethodParameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RequestParamArgumentResolver implements ArgumentResolver {

    @Override
    public boolean supports(MethodParameter methodParameter) {
        return methodParameter.isParameterAnnotationPresent(RequestParam.class);
    }

    @Override
    public Object resolveArgument(HttpServletRequest request, HttpServletResponse response, MethodParameter methodParameter) {
        RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
        String argument = getArgument(request, requestParam);
        if (requestParam.required() && argument == null) {
            throw new IllegalArgumentException("존재하지 않는 파라미터입니다.");
        }
        Class<?> type = methodParameter.getType();
        return StringConverter.convert(type, argument);
    }

    private String getArgument(HttpServletRequest request, RequestParam requestParam) {
        String name = requestParam.value();
        return request.getParameter(name);
    }
}
