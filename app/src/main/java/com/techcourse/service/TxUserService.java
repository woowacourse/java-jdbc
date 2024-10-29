package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final AppUserService appUserService;

    public TxUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        return appUserService.findById(id);
    }

    @Override
    public void save(User user) {
        appUserService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            appUserService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                log.error(ex.getMessage(), ex);
            }
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
