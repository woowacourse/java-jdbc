package nextstep.mvc.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import nextstep.mvc.exception.InternalServerException;
import nextstep.mvc.view.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerExecution {

    private static final Logger LOG = LoggerFactory.getLogger(HandlerExecution.class);

    private final Object declaredObject;
    private final Method method;

    public HandlerExecution(Object declaredObject, Method method) {
        this.declaredObject = declaredObject;
        this.method = method;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
        throws Throwable {
        try {
            return (ModelAndView) method.invoke(declaredObject, request, response);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException | IllegalArgumentException e) {
            LOG.error("{} method invoke fail. error message : {}", method, e.getMessage());
            throw new InternalServerException();
        }
    }
}
