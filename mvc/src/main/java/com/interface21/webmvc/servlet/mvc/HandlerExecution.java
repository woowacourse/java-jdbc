package com.interface21.webmvc.servlet.mvc;

import com.interface21.bean.container.BeanContainer;
import java.lang.reflect.Method;

public class HandlerExecution {

    private final Method handler;

    public HandlerExecution(Method handler) {
        this.handler = handler;
    }

    public Object handle(Object... args) throws Exception {
        Class<?> clazz = handler.getDeclaringClass();
        Object controller = BeanContainer.getInstance().getBean(clazz);

        return handler.invoke(controller, args);
    }

    public Method getMethod() {
        return handler;
    }
}
