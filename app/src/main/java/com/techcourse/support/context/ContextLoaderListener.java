package com.techcourse.support.context;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import nextstep.jdbc.JdbcTemplate;

@WebListener
public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabasePopulatorUtils.execute(new JdbcTemplate(DataSourceConfig.getInstance()));
    }
}
