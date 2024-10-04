package com.interface21.webmvc.servlet.mvc.handleradaptor;

import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.HandlerAdapter;
import com.interface21.webmvc.servlet.mvc.HandlerExecution;
import com.interface21.webmvc.servlet.mvc.argumentresolver.ArgumentResolvers;
import com.interface21.webmvc.servlet.mvc.returnvaluehandler.ReturnValueHandlers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class HandlerExecutionHandlerAdapter implements HandlerAdapter {

    private static final ReturnValueHandlers returnvalueHandlers = new ReturnValueHandlers();
    private static final ArgumentResolvers argumentResolvers = new ArgumentResolvers();

    @Override
    public boolean supports(Object handler) {
        return (handler instanceof HandlerExecution);
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HandlerExecution handlerExecution = (HandlerExecution) handler;
        Method method = handlerExecution.getMethod();
        Object[] args = argumentResolvers.handle(request, response, method);
        Object returnValue = handlerExecution.handle(args);
        return returnvalueHandlers.handle(returnValue);
    }
}
