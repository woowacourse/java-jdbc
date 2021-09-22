package com.techcourse.support.context;

import com.techcourse.config.DataSourceConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import nextstep.datasource.DatabasePopulatorUtils;

import java.net.URL;

@WebListener
public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        URL sqlUrl = getClass().getClassLoader().getResource("schema.sql");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance(), sqlUrl);
    }
}
