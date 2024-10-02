package com.interface21.webmvc.servlet;

import com.interface21.core.BeanContainer;
import com.interface21.core.BeanContainerFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HandlerExecution {

    private final Method method;

    public HandlerExecution(Method method) {
        this.method = method;
    }

    public Object handle(Object... args) throws InvocationTargetException, IllegalAccessException {
        BeanContainer container = BeanContainerFactory.getContainer();
        Class<?> controllerClass = method.getDeclaringClass();
        Object controller = container.getBean(controllerClass);
        return method.invoke(controller, args);
    }

    public Parameter[] getParameters() {
        return method.getParameters();
    }
}
