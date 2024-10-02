package com.interface21.webmvc.servlet.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public interface ArgumentResolver {

    boolean supports(Parameter parameter);

    Object resolveArgument(HttpServletRequest request, HttpServletResponse response, Parameter parameter);
}
