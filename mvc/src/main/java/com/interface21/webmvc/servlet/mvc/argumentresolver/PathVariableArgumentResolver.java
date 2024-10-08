package com.interface21.webmvc.servlet.mvc.argumentresolver;

import com.interface21.core.util.StringConverter;
import com.interface21.web.bind.annotation.PathVariable;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.webmvc.servlet.mvc.ArgumentResolver;
import com.interface21.webmvc.servlet.mvc.MethodParameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.IntStream;

public class PathVariableArgumentResolver implements ArgumentResolver {

    private static final String PATH_VARIABLE_FORMAT = "{%s}";
    private static final String PATH_DELIMITER = "/";
    private static final int INVALID_INDEX = -1;

    @Override
    public boolean supports(MethodParameter methodParameter) {
        return methodParameter.isMethodAnnotationPresent(RequestMapping.class)
               && methodParameter.isParameterAnnotationPresent(PathVariable.class);
    }

    @Override
    public Object resolveArgument(
            HttpServletRequest request, HttpServletResponse response, MethodParameter methodParameter
    ) {
        RequestMapping requestMapping = methodParameter.getMethodAnnotation(RequestMapping.class);
        PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);

        int index = findPathVariableIndex(requestMapping, pathVariable);
        String value = getValue(request, pathVariable, index);

        Class<?> type = methodParameter.getType();
        return StringConverter.convert(type, value);
    }

    private int findPathVariableIndex(RequestMapping requestMapping, PathVariable pathVariable) {
        String endPoint = requestMapping.value();
        String valueName = pathVariable.value();
        String pathFormat = String.format(PATH_VARIABLE_FORMAT, valueName);

        String[] pathVariables = endPoint.split(PATH_DELIMITER);

        return IntStream.range(0, pathVariables.length)
                .filter(i -> pathVariables[i].equals(pathFormat))
                .findFirst()
                .orElse(INVALID_INDEX);
    }

    private String getValue(HttpServletRequest request, PathVariable pathVariable, int index) {
        if (pathVariable.required() && index == INVALID_INDEX) {
            throw new IllegalArgumentException("현재 경로에 존재하지 않는 값입니다.");
        }
        return index == INVALID_INDEX ? null : request.getRequestURI().split(PATH_DELIMITER)[index];
    }
}
