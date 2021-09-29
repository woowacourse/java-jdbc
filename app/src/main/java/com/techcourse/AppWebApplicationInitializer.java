package com.techcourse;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import nextstep.mvc.DispatcherServlet;
import nextstep.mvc.adapter.HandlerExecutionHandlerAdapter;
import nextstep.mvc.handler.AnnotationExceptionMapping;
import nextstep.mvc.handler.AnnotationHandlerMapping;
import nextstep.web.WebApplicationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppWebApplicationInitializer implements WebApplicationInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AppWebApplicationInitializer.class);
    private static final String BASE_PACKAGE = "com.techcourse";

    @Override
    public void onStartup(ServletContext servletContext) {
        final DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.addHandlerMapping(new AnnotationHandlerMapping(BASE_PACKAGE));
        dispatcherServlet.addHandlerAdapter(new HandlerExecutionHandlerAdapter());
        dispatcherServlet.addExceptionHandlerMapping(new AnnotationExceptionMapping(BASE_PACKAGE));

        final ServletRegistration.Dynamic dispatcher = servletContext
            .addServlet("dispatcher", dispatcherServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        LOG.info("Start AppWebApplication Initializer");
    }
}
