package nextstep.mvc.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import nextstep.mvc.exception.ReflectionException;
import nextstep.mvc.view.ModelAndView;

public class HandlerExecution {

    private final Object declaredObject;
    private final Method method;

    public HandlerExecution(final Object declaredObject, final Method method) {
        this.declaredObject = declaredObject;
        this.method = method;
    }

    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            return (ModelAndView) method.invoke(declaredObject, request, response);
        } catch (final InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException("Failed to invoke the underlying method.", e);
        }
    }

    @Override
    public String toString() {
        return "Instance : " + declaredObject.getClass() +
                ", Method : " + method.getName();
    }
}
