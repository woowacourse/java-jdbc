package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HandlerExecution {

    private final Method handler;
    private final Object controller;

    public HandlerExecution(final Method handler, final Object controller) {
        this.handler = handler;
        this.controller = controller;
    }

    public Object handle(final HttpServletRequest request, final HttpServletResponse response)
            throws IllegalAccessException, InvocationTargetException {
        return handler.invoke(controller, request, response);
    }
}
