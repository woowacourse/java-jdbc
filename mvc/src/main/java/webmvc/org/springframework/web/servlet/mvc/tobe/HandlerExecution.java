package webmvc.org.springframework.web.servlet.mvc.tobe;

import java.lang.reflect.Method;

public class HandlerExecution {

    private final Object bean;
    private final Method method;

    public HandlerExecution(final Object bean, final Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }
}
