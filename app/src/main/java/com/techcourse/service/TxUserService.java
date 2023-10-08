package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(UserService userService) {
        this.dataSource = DataSourceConfig.getInstance();
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            Connection conn = DataSourceUtils.getConnection(dataSource);
            try {
                conn.setAutoCommit(false);
                userService.changePassword(id, newPassword, createBy);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw SQLExceptionTranslator.translate("", e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }
}

