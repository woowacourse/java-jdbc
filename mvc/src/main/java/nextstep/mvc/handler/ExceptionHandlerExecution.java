package nextstep.mvc.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import nextstep.mvc.exception.InternalServerException;
import nextstep.mvc.view.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandlerExecution {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerExecution.class);

    private final Object declaredObject;
    private final Method method;

    public ExceptionHandlerExecution(Object declaredObject, Method method) {
        this.declaredObject = declaredObject;
        this.method = method;
    }

    public ModelAndView handle(Exception exception) {
        try {
            return (ModelAndView) method.invoke(declaredObject, exception);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.error("{} method invoke fail. error message : {}", method, e.getMessage());
            throw new InternalServerException();
        }
    }
}
