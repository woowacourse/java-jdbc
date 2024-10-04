package com.interface21.webmvc.servlet.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ArgumentResolver {

    boolean supports(MethodParameter methodParameter);

    Object resolveArgument(HttpServletRequest request, HttpServletResponse response, MethodParameter methodParameter);
}
