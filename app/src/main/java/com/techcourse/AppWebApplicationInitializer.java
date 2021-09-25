package com.techcourse;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import nextstep.mvc.DispatcherServlet;
import nextstep.mvc.controller.AnnotationHandlerAdapter;
import nextstep.mvc.controller.AnnotationHandlerMapping;
import nextstep.mvc.exception.ComponentContainerException;
import nextstep.web.ComponentContainer;
import nextstep.web.WebApplicationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppWebApplicationInitializer implements WebApplicationInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AppWebApplicationInitializer.class);
    private static final String BASE_PACKAGE = "com.techcourse";

    @Override
    public void onStartup(ServletContext servletContext) {
        try {
            LOG.info("Start Components Initializer");
            ComponentContainer.initializeComponents(BASE_PACKAGE);
        } catch (Exception e) {
            LOG.error("Initialize Components Error: {}", e.getMessage());
            throw new ComponentContainerException();
        }

        final DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.addHandlerMapping(new AnnotationHandlerMapping(BASE_PACKAGE));
        dispatcherServlet.addHandlerAdapter(new AnnotationHandlerAdapter());

        final ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        LOG.info("Start AppWebApplication Initializer");
    }
}
