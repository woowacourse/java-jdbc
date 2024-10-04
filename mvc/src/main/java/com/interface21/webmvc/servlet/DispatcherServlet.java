package com.interface21.webmvc.servlet;

import com.interface21.webmvc.servlet.mvc.HandlerAdapter;
import com.interface21.webmvc.servlet.mvc.handleradaptor.HandlerAdapters;
import com.interface21.webmvc.servlet.mvc.handlermapping.HandlerMappings;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private HandlerMappings handlerMappings;
    private HandlerAdapters handlerAdapters;

    public DispatcherServlet() {
    }

    @Override
    public void init() {
        handlerAdapters = new HandlerAdapters();
        handlerMappings = new HandlerMappings();
        handlerMappings.initialize();
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException {
        log.debug("Method : {}, Request URI : {}", request.getMethod(), request.getRequestURI());
        try {
            Optional<Object> optionalHandler = handlerMappings.findHandler(request);
            if (optionalHandler.isEmpty()) {
                response.setStatus(404);
                return;
            }

            Object handler = optionalHandler.get();
            Optional<HandlerAdapter> optionalHandlerAdapter = handlerAdapters.findHandlerAdaptor(handler);
            if (optionalHandlerAdapter.isEmpty()) {
                response.setStatus(404);
                return;
            }

            ModelAndView modelAndView = optionalHandlerAdapter.get().handle(request, response, handler);
            move(modelAndView, request, response);
        } catch (Throwable e) {
            log.error("Exception : {}", e.getMessage(), e);
            throw new ServletException(e.getMessage());
        }
    }

    private void move(ModelAndView modelAndView, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = modelAndView.getModel();
        View view = modelAndView.getView();
        view.render(model, request, response);
    }
}
