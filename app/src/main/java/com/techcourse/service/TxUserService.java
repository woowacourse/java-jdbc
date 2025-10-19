package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.JdbcException;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService target;
    private final DataSource dataSource;

    public TxUserService(
            final UserService target,
            final DataSource dataSource
    ) {
        this.target = target;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return target.findById(id);
    }

    @Override
    public void insert(final User user) {
        target.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            target.changePassword(id, newPassword, createBy);
            conn.commit();
        } catch (SQLException e) {
            connectionRollback(conn);
            throw new JdbcException(e);
        } catch (Exception e) {
            connectionRollback(conn);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void connectionRollback(final Connection conn) {
        if (conn != null) {
            try {
                log.warn("rollback");
                conn.rollback();
            } catch (SQLException exception) {
                throw new JdbcException(exception);
            }
        }
    }
}
