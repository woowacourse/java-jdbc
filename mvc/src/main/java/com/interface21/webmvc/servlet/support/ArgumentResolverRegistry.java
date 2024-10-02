package com.interface21.webmvc.servlet.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class ArgumentResolverRegistry {

    private final List<ArgumentResolver> resolvers = List.of(
            new HttpServletRequestArgumentResolver(),
            new HttpServletResponseArgumentResolver()
    );

    public Object[] resolveArguments(HttpServletRequest request, HttpServletResponse response, Parameter[] parameters) {
        return Arrays.stream(parameters)
                .map(parameter -> resolveArgument(request, response, parameter))
                .toArray();
    }

    public Object resolveArgument(HttpServletRequest request, HttpServletResponse response, Parameter parameter) {
        return resolvers.stream()
                .filter(resolver -> resolver.supports(parameter))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not resolve parameter: " + parameter))
                .resolveArgument(request, response, parameter);
    }
}
