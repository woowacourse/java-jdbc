package com.interface21.webmvc.servlet.mvc.argumentresolver;

import com.interface21.bean.container.BeanContainer;
import com.interface21.webmvc.servlet.mvc.ArgumentResolver;
import com.interface21.webmvc.servlet.mvc.MethodParameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.IntStream;

public class ArgumentResolvers {

    private final List<ArgumentResolver> argumentResolvers;

    public ArgumentResolvers() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        this.argumentResolvers = beanContainer.getSubTypeBeansOf(ArgumentResolver.class);
    }

    public Object[] handle(HttpServletRequest request, HttpServletResponse response, Method method) {
        return IntStream.range(0, method.getParameterCount())
                .mapToObj(index -> new MethodParameter(method, index))
                .map(methodParameter -> getArgument(request, response, methodParameter))
                .toArray();
    }

    private Object getArgument(HttpServletRequest request, HttpServletResponse response, MethodParameter methodParameter) {
        ArgumentResolver argumentResolver = findArgumentResolver(methodParameter);
        return argumentResolver.resolveArgument(request, response, methodParameter);
    }

    private ArgumentResolver findArgumentResolver(MethodParameter methodParameter) {
        return argumentResolvers.stream()
                .filter(argumentResolver -> argumentResolver.supports(methodParameter))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("변환 불가능한 타입입니다."));
    }
}
