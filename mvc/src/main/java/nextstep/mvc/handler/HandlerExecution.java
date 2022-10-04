package nextstep.mvc.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.reflections.Reflections;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.argumentResolver.ArgumentResolver;
import nextstep.mvc.argumentResolver.ArgumentResolverMapping;
import nextstep.mvc.view.ModelAndView;
import nextstep.web.annotation.RequestParam;

public class HandlerExecution {

    private final Method method;

    public HandlerExecution(Method method) {
        this.method = method;
    }

    public Object handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        Object handler = createHandlerInstance(method.getDeclaringClass());
        Object[] parameters = Arrays.stream(method.getParameters())
            .map(type -> resolveArgument(request, response, type))
            .toArray();
        return method.invoke(handler, parameters);
    }

    private Object resolveArgument(HttpServletRequest request, HttpServletResponse response, Parameter parameter) {
        ArgumentResolver argumentResolver = ArgumentResolverMapping.getArgumentResolver(parameter);
        return argumentResolver.resolve(request, response, parameter);
    }

    private Object createHandlerInstance(Class<?> handler) {
        try {
            return handler.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }
}
