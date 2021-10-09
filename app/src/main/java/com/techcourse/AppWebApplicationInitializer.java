package com.techcourse;

import context.ApplicationContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import nextstep.datasource.DatabasePopulator;
import nextstep.mvc.DispatcherServlet;
import nextstep.mvc.controller.asis.ControllerHandlerAdapter;
import nextstep.mvc.controller.tobe.AnnotationHandlerMapping;
import nextstep.mvc.controller.tobe.HandlerExecutionHandlerAdapter;
import nextstep.web.WebApplicationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class AppWebApplicationInitializer implements WebApplicationInitializer {

    private static final Logger log = LoggerFactory.getLogger(AppWebApplicationInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        ApplicationContext applicationContext = new ApplicationContext("com.techcourse");

        DatabasePopulator databasePopulator = (DatabasePopulator) applicationContext.takeComponent(DatabasePopulator.class);
        URL sqlUrl = getClass().getClassLoader().getResource("schema.sql");
        databasePopulator.execute(sqlUrl);

        final DispatcherServlet dispatcherServlet = new DispatcherServlet();

        dispatcherServlet.addHandlerMapping(new ManualHandlerMapping());
        dispatcherServlet.addHandlerMapping(new AnnotationHandlerMapping(applicationContext.findController()));

        dispatcherServlet.addHandlerAdapter(new ControllerHandlerAdapter());
        dispatcherServlet.addHandlerAdapter(new HandlerExecutionHandlerAdapter());

        final ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        log.info("Start AppWebApplication Initializer");
    }
}
