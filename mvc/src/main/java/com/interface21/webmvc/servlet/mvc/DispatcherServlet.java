package com.interface21.webmvc.servlet.mvc;

import com.interface21.web.http.StatusCode;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.mvc.exception.NotFoundHandlerException;
import com.interface21.webmvc.servlet.mvc.exception.WebMvcServletException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private final HandlerMappingRegistry handlerMappingRegistry;
    private final HandlerAdapterRegistry handlerAdapterRegistry;
    private HandlerExecutor handlerExecutor;

    public DispatcherServlet() {
        handlerMappingRegistry = new HandlerMappingRegistry();
        handlerAdapterRegistry = new HandlerAdapterRegistry();
    }

    @Override
    public void init() {
        handlerExecutor = new HandlerExecutor(handlerAdapterRegistry);
    }

    public void addHandlerMapping(final HandlerMapping handlerMapping) {
        handlerMappingRegistry.addHandlerMapping(handlerMapping);
    }

    public void addHandlerAdapter(final HandlerAdapter handlerAdapter) {
        handlerAdapterRegistry.addHandlerAdapter(handlerAdapter);
    }

    @Override
    protected void service(final HttpServletRequest request,
                           final HttpServletResponse response) throws ServletException, IOException {
        logRequest(request);
        try {
            final Object handler = getHandler(request);
            final ModelAndView modelAndView = handlerExecutor.handle(request, response, handler);
            render(modelAndView, request, response);
        } catch (WebMvcServletException e) {
            handleWebMvcServletException(e, response);
        } catch (Throwable e) {
            handleException(e);
        }
    }

    private void logRequest(final HttpServletRequest request) {
        log.info("Method : {}, Request URI : {}", request.getMethod(), request.getRequestURI());
    }

    private Object getHandler(final HttpServletRequest request) {
        return handlerMappingRegistry.findHandler(request)
                .orElseThrow(NotFoundHandlerException::new);
    }

    private void render(final ModelAndView modelAndView,
                        final HttpServletRequest request,
                        final HttpServletResponse response) throws Exception {
        final var view = modelAndView.getView();
        view.render(modelAndView.getModel(), request, response);
    }

    private void handleWebMvcServletException(final WebMvcServletException exception,
                                              final HttpServletResponse response) throws IOException {
        StatusCode statusCode = exception.getStatusCode();
        String message = exception.getMessage();
        response.sendError(statusCode.value(), message);
        if (statusCode.is4xxClientError()) {
            log.info("WebMvcServletException : {}", message, exception);
            return;
        }
        if (statusCode.is5xxServerError()) {
            log.error("WebMvcServletException : {}", message, exception);
            return;
        }
        log.debug("WebMvcServletException : {}", message, exception);
    }

    private void handleException(final Throwable e) throws ServletException {
        log.error("Exception : {}", e.getMessage(), e);
        throw new ServletException(e.getMessage());
    }
}
