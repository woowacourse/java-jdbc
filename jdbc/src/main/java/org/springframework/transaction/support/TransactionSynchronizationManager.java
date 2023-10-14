package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> isActive = ThreadLocal.withInitial(() -> false);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource dataSource) throws SQLException {
        if (!isActive.get() || !resources.get().containsKey(dataSource)) {
            throw new RuntimeException("시작한 트랜잭션이 없습니다.");
        }
        return resources.get().get(dataSource);
    }

    private static Connection bindResource(final DataSource dataSource) {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            resources.get().put(dataSource, connection);
            return connection;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startNewTransaction(final DataSource dataSource) {
        // TODO: 2023/10/14 transaction이 active인 경우 기존 트랜잭션 참여 여부에 따라 새로 생성하거나 기존 트랜잭션에 참여하도록 하는 것도 필요할까?
        isActive.set(true);
        bindResource(dataSource);
    }

    public static void commitTransaction(final DataSource dataSource) {
        try {
            final Connection connection = resources.get().get(dataSource);
            connection.commit();
        } catch (final SQLException ex) {
            rollback(dataSource);

            throw new DataAccessException("실행 중 예외가 발생했습니다.");
        }
    }

    public static void rollback(final DataSource dataSource) {
        try {
            if (!isActive.get()) {
                final Connection connection = resources.get().get(dataSource);
                connection.rollback();

                clear(dataSource);
            }
        } catch (final SQLException ex) {
            throw new DataAccessException("트랜잭션 롤백 중 예외가 발생했습니다.");
        }
    }

    private static void clear(final DataSource dataSource) {
        try {
            final Connection connection = resources.get().get(dataSource);
            connection.setAutoCommit(true);
            connection.close();
            DataSourceUtils.releaseConnection(connection);
        } catch (final SQLException ex) {
            throw new DataAccessException("실행 중 예외가 발생했습니다.");
        }
    }

    public static void finishTransaction(final DataSource dataSource) {
        isActive.set(false);
        unbindResource(dataSource);
        clear(dataSource);
    }

    public static Connection unbindResource(final DataSource dataSource) {
        return resources.get().remove(dataSource);
    }
}
