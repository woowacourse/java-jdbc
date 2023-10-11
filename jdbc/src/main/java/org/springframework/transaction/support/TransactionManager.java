package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.SQLException;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() {
        try {
            final var connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() {
        try {
            final var connection = DataSourceUtils.getConnection(dataSource);
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void rollback() {
        try {
            final var connection = DataSourceUtils.getConnection(dataSource);
            connection.rollback();
        } catch (SQLException e) {
            log.error("트랜잭션 수행 과정에서 오류가 발생했습니다 => Rollback");
            throw new DataAccessException(e);
        }
    }

    public void end() {
        final var connection = DataSourceUtils.getConnection(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}
