package com.techcourse.support.context;

import com.techcourse.config.AppConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.sql.DataSource;

@WebListener
public class ContextLoaderListener implements ServletContextListener {

    private final DataSource dataSource;
    private final UserDao userDao;

    public ContextLoaderListener() {
        this(AppConfig.getInstance().getDataSource(), AppConfig.getInstance().getUserDao());
    }

    public ContextLoaderListener(final DataSource dataSource, final UserDao userDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        DatabasePopulatorUtils.execute(dataSource);
        if (userDao.findByAccount("gugu") == null) {
            userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        }
    }
}
