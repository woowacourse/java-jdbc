package com.interface21.webmvc.servlet;

import com.interface21.webmvc.servlet.mvc.handler.AnnotationHandlerMapping;
import com.interface21.webmvc.servlet.mvc.handler.AnnotationMethodHandlerAdapter;
import com.interface21.webmvc.servlet.mvc.handler.HandlerAdapter;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerMapping;
import com.interface21.webmvc.servlet.view.InternalResourceViewResolver;
import com.interface21.webmvc.servlet.view.ModelAndView;
import com.interface21.webmvc.servlet.view.View;
import com.interface21.webmvc.servlet.view.ViewResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    public static final String DEFAULT_BASE_PACKAGE = "com.techcourse.controller";

    private HandlerMapping handlerMapping;
    private HandlerAdapter handlerAdapter;
    private ViewResolver viewResolver;

    public DispatcherServlet() {
    }

    @Override
    public void init() {
        this.handlerMapping = new AnnotationHandlerMapping(DEFAULT_BASE_PACKAGE);
        this.handlerMapping.initialize();
        this.viewResolver = new InternalResourceViewResolver();
        this.handlerAdapter = new AnnotationMethodHandlerAdapter(this.viewResolver);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        log.debug("Method : {}, Request URI : {}", request.getMethod(), request.getRequestURI());
        try {
            Object handler = handlerMapping.getHandler(request);
            ModelAndView modelAndView = handlerAdapter.handle(handler, request, response);
            render(modelAndView, request, response);
        } catch (Exception exception) {
            log.error("Exception : {}", exception.getMessage(), exception);
            throw new ServletException(exception);
        }
    }

    private void render(ModelAndView modelAndView, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        View view = modelAndView.getView();
        view.render(modelAndView.getModel(), request, response);
    }
}
