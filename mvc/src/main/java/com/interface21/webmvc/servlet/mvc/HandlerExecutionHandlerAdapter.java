package com.interface21.webmvc.servlet.mvc;

import com.interface21.webmvc.servlet.HandlerAdapter;
import com.interface21.webmvc.servlet.HandlerExecution;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.support.ArgumentResolverRegistry;
import com.interface21.webmvc.servlet.support.ReturnValueAdapterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class HandlerExecutionHandlerAdapter implements HandlerAdapter {

    private final ArgumentResolverRegistry argumentResolverRegistry = new ArgumentResolverRegistry();
    private final ReturnValueAdapterRegistry returnValueAdapterRegistry = new ReturnValueAdapterRegistry();

    @Override
    public boolean supports(Object handler) {
        return handler instanceof HandlerExecution;
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Parameter[] parameters = ((HandlerExecution) handler).getParameters();
        Object[] resolvedArguments = argumentResolverRegistry.resolveArguments(request, response, parameters);
        Object returnValue = ((HandlerExecution) handler).handle(resolvedArguments);
        return returnValueAdapterRegistry.adapt(returnValue);
    }
}
