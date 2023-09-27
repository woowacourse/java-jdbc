package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import web.org.springframework.web.bind.annotation.RequestMapping;
import webmvc.org.springframework.web.servlet.ModelAndView;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RequestMappingHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(final Object handler) {
        final Method method = (Method) handler;
        final Class<?> handlerClass = method.getDeclaringClass();

        return handlerClass.isAnnotationPresent(RequestMapping.class) ||
                isCustomRequestMappingPresent(method);
    }

    private boolean isCustomRequestMappingPresent(final Method handlerMethod) {
        final Annotation[] annotations = handlerMethod.getDeclaredAnnotations();

        return Arrays.stream(annotations)
                .map(Annotation::annotationType)
                .anyMatch(annotationType -> annotationType.isAnnotationPresent(RequestMapping.class));
    }

    @Override
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
                               final Object handler) {
        final HandlerExecution handlerExecution = (HandlerExecution) handler;
        final Object bean = handlerExecution.getBean();
        final Method method = handlerExecution.getMethod();

        try {
            return (ModelAndView) method.invoke(bean, request, response);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new MethodInvocationException("어노테이션 메소드를 실행하는 도중 예외가 발생했습니다.", e);
        }
    }
}
