package webmvc.org.springframework.web.servlet.mvc.tobe;

import web.org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class HandlerExecutions {

    private final Map<HandlerKey, HandlerExecution> mappings;

    public HandlerExecutions(Map<HandlerKey, HandlerExecution> mappings) {
        this.mappings = mappings;
    }

    public void addHandlerExecution(final String requestPath, final RequestMethod[] requestMethods,
                                    final Method method) {
        final Object bean = instantiate(method.getDeclaringClass());
        final HandlerExecution handlerExecution = new HandlerExecution(bean, method);

        for (RequestMethod requestMethod : requestMethods) {
            HandlerKey handlerKey = new HandlerKey(requestPath, requestMethod);
            mappings.put(handlerKey, handlerExecution);
        }
    }

    private Object instantiate(final Class<?> clazz) {
        try {
            return clazz.getConstructor()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new InstantiationFailedException("인스턴스화를 하는 도중 예외가 발생했습니다.", e);
        }
    }

    public HandlerExecution getHandlerExecutions(HandlerKey handlerKey) {
        return mappings.get(handlerKey);
    }
}
