package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AnnotationHandler implements Handler {

    private final HandlerExecution handlerExecution;

    public AnnotationHandler(final HandlerExecution handlerExecution) {
        this.handlerExecution = handlerExecution;
    }

    @Override
    public boolean isSupport() {
        return handlerExecution != null;
    }

    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return handlerExecution.handle(request, response);
    }
}
