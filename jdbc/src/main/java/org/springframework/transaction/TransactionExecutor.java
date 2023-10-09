package org.springframework.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.SQLException;

public class TransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(TransactionExecutor.class);
    private final DataSource dataSource;

    public TransactionExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionalOperationWithReturn<T> operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            T result = operation.execute();
            transaction.commit();
            return result;
        } catch (RuntimeException applicationException) {
            safeRollback(transaction);
            throw applicationException;
        } catch (SQLException sqlException) {
            safeRollback(transaction);
            log.error("트랜잭션 처리 중 오류 발생", sqlException);
            throw new DataAccessException(sqlException);
        } finally {
            DataSourceUtils.releaseConnectionOf(dataSource);
        }
    }

    public void execute(TransactionalOperation operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            operation.execute();
            transaction.commit();
        } catch (RuntimeException applicationException) {
            safeRollback(transaction);
            throw applicationException;
        } catch (SQLException sqlException) {
            safeRollback(transaction);
            log.error("트랜잭션 처리 중 오류 발생", sqlException);
            throw new DataAccessException(sqlException);
        } finally {
            DataSourceUtils.releaseConnectionOf(dataSource);
        }
    }

    private void safeRollback(Transaction transaction) {
        try {
            transaction.rollback();
        } catch (SQLException e) {
            log.error("트랜잭션 롤백 중 오류 발생", e);
            throw new DataAccessException(e);
        }
    }

    private Transaction getTransaction() {
        return new Transaction(DataSourceUtils.getConnection(dataSource));
    }
}
