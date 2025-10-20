package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(final long id) {
        return processTransaction(() -> userService.findById(id));
    }

    @Override
    public User findByAccount(final String account) {
        return processTransaction(() -> userService.findByAccount(account));
    }

    @Override
    public void save(final User user) {
        processTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        processTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private <T> T processTransaction(Callable<T> transactionCallback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            final T result = transactionCallback.call();

            connection.commit();
            return result;
        } catch (final Exception e) {
            rollback(connection);
            log.warn("sql 실행 중 오류 발생: rollback 완료", e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void processTransaction(Runnable transactionCallback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            transactionCallback.run();

            connection.commit();
        } catch (final Exception e) {
            rollback(connection);
            log.warn("sql 실행 중 오류 발생: rollback 완료", e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(final Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
                return;
            }
            log.warn("connection 없음");
        } catch (final SQLException e) {
            log.error("sql 실행 중 오류 발생: rollback 실패", e);
            throw new DataAccessException(e);
        }
    }
}
