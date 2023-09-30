package webmvc.org.springframework.web.servlet.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import webmvc.org.springframework.web.servlet.ModelAndView;

public class HandlerExecutor {

    private final HandlerAdapterRegistry handlerAdapterRegistry;

    public HandlerExecutor(HandlerAdapterRegistry handlerAdapterRegistry) {
        this.handlerAdapterRegistry = handlerAdapterRegistry;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerAdapter handlerAdapter = handlerAdapterRegistry.getHandlerAdapter(handler);
        return handlerAdapter.handle(request, response, handler);
    }
}
