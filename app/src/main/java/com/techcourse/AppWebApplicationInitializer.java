package com.techcourse;

import jakarta.servlet.ServletContext;
import webmvc.org.springframework.web.servlet.mvc.tobe.AnnotationHandlerAdapter;
import webmvc.org.springframework.web.servlet.mvc.tobe.AnnotationHandlerMapping;
import web.org.springframework.web.WebApplicationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webmvc.org.springframework.web.servlet.mvc.tobe.DispatcherServlet;
import webmvc.org.springframework.web.servlet.mvc.tobe.HandlerAdapters;
import webmvc.org.springframework.web.servlet.mvc.tobe.HandlerMappings;
import webmvc.org.springframework.web.servlet.mvc.tobe.ManualHandlerAdapter;

public class AppWebApplicationInitializer implements WebApplicationInitializer {

    private static final Logger log = LoggerFactory.getLogger(AppWebApplicationInitializer.class);

    @Override
    public void onStartup(final ServletContext servletContext) {
        final HandlerMappings handlerMappings=new HandlerMappings(new ManualHandlerMapping(),new AnnotationHandlerMapping("com.techcourse.controller"));
        final HandlerAdapters handlerAdapters=new HandlerAdapters(new ManualHandlerAdapter(),new AnnotationHandlerAdapter());
        final var dispatcherServlet = new DispatcherServlet(handlerMappings,handlerAdapters);

        final var dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        log.info("Start AppWebApplication Initializer");
    }
}
