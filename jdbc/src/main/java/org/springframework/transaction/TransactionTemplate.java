package org.springframework.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void doTransaction(final Consumer<Connection> consumer) {
        try (final Connection conn = dataSource.getConnection();) {
            doInternalTransaction(consumer, conn);
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e);
        }
    }

    private void doInternalTransaction(final Consumer<Connection> consumer, final Connection conn) throws SQLException {
        try {
            conn.setAutoCommit(false);

            consumer.accept(conn);

            conn.commit();
        } catch (SQLException | DataAccessException e) {
            conn.rollback();
            log.warn("트랜잭션 내에서 오류가 발생하여 롤백합니다.", e);
            throw new DataAccessException(e);
        }
    }
}
