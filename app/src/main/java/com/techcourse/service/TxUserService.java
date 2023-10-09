package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TxUserService implements UserService {

    private final UserService delegate;

    public TxUserService(UserService delegate) {
        this.delegate = delegate;
    }

    public User findById(final long id) {
        return delegate.findById(id);
    }

    public void insert(final User user) {
        delegate.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
            try {
                conn.setAutoCommit(false);
                delegate.changePassword(id, newPassword, createBy);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException(e);
            } finally {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
