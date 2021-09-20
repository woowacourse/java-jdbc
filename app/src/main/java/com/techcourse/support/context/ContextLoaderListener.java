package com.techcourse.support.context;

import nextstep.jdbc.datasource.DataSourceConfig;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }
}
