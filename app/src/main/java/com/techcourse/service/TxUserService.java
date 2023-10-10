package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final DataSource datasource = DataSourceConfig.getInstance();

    public TxUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        return execute(() -> appUserService.findById(id));
    }

    @Override
    public void insert(User user) {
        execute(() -> appUserService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        execute(() -> appUserService.changePassword(id, newPassword, createBy));
    }

    private void execute(ExecutableWithoutReturn executable) {
        try (TransactionExecutor executor = TransactionExecutor.initTransaction(datasource)) {
            try {
                executable.execute();
                executor.commit();
            } catch (Exception e) {
                executor.rollback();
                throw new DataAccessException();
            }
        }
    }

    private <T> T execute(ExecutableWithReturn<T> executable) {
        try (TransactionExecutor transactionExecutor = TransactionExecutor.initTransaction(datasource)) {
            try {
                T result = executable.execute();
                transactionExecutor.commit();
                return result;
            } catch (Exception e) {
                transactionExecutor.rollback();
                throw new DataAccessException();
            }
        }
    }

    @FunctionalInterface
    private interface ExecutableWithoutReturn {
        void execute();
    }

    @FunctionalInterface
    private interface ExecutableWithReturn<T> {
        T execute();
    }

    private static class TransactionExecutor implements AutoCloseable {

        private final DataSource dataSource;
        private final Connection connection;

        private TransactionExecutor(DataSource dataSource, Connection connection) {
            this.dataSource = dataSource;
            this.connection = connection;
        }

        public static TransactionExecutor initTransaction(DataSource dataSource) {
            try {
                Connection conn = dataSource.getConnection();
                TransactionSynchronizationManager.bindResource(dataSource, conn);
                Connection connection = DataSourceUtils.getConnection(dataSource);
                connection.setAutoCommit(false);
                return new TransactionExecutor(dataSource, connection);
            } catch (SQLException e) {
                throw new RuntimeException();
            }
        }

        public void commit() {
            try {
                connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException();
            }
        }

        public void rollback() {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RuntimeException();
            }
        }

        @Override
        public void close() {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
