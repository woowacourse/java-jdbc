package com.techcourse.support.context;

import com.techcourse.JwpApplication;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ContextLoaderListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(JwpApplication.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        log.info("database populated");
    }
}
