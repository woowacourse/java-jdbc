package com.techcourse;

import com.interface21.core.util.DIContainer;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.AppUserService;
import com.techcourse.service.UserService;
import jakarta.servlet.ServletContext;
import com.interface21.webmvc.servlet.mvc.DispatcherServlet;
import com.interface21.webmvc.servlet.mvc.asis.ControllerHandlerAdapter;
import com.interface21.webmvc.servlet.mvc.tobe.AnnotationHandlerMapping;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerExecutionHandlerAdapter;
import com.interface21.web.WebApplicationInitializer;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppWebApplicationInitializer implements WebApplicationInitializer {

    private static final Logger log = LoggerFactory.getLogger(AppWebApplicationInitializer.class);

    @Override
    public void onStartup(final ServletContext servletContext) {
        DIContainer diContainer = new DIContainer();

        DataSource dataSource = DataSourceConfig.getInstance();
        diContainer.registerBean(DataSource.class, dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        diContainer.registerBean(JdbcTemplate.class, jdbcTemplate);

        UserDao userDao = new UserDao(jdbcTemplate);
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        diContainer.registerBean(UserService.class, appUserService);

        diContainer.scanAndRegister("com.techcourse");

        final var dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.addHandlerMapping(new ManualHandlerMapping());
        dispatcherServlet.addHandlerMapping(new AnnotationHandlerMapping(diContainer, "com.techcourse.controller"));

        dispatcherServlet.addHandlerAdapter(new ControllerHandlerAdapter());
        dispatcherServlet.addHandlerAdapter(new HandlerExecutionHandlerAdapter());

        final var dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        log.info("Start AppWebApplication Initializer");
    }
}
