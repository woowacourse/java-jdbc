package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.RevertAutoCommitFailException;
import com.interface21.dao.RollbackFailedException;
import com.interface21.dao.TransactionRollbackException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void injectTransaction(Consumer<Connection> action) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        runTransactionWithAtomicity(action, conn);
    }

    private void runTransactionWithAtomicity(Consumer<Connection> action, Connection conn) {
        try {
            conn.setAutoCommit(false);
            action.accept(conn);
            conn.commit();
        } catch (SQLException | DataAccessException e) {
            tryRollBack(conn);
            throw new TransactionRollbackException("트랜잭션 수행 중 문제가 발생하여 롤백하였습니다.", e);
        } finally {
            revertTransactionAutocommit(conn);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void revertTransactionAutocommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RevertAutoCommitFailException("트랜잭션 자동 커밋 복구 중 예외가 발생하였습니다.",e);
        }
    }

    private void tryRollBack(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new RollbackFailedException("롤백 작업 수행 중 문제가 발생하였습니다.", e);
        }
    }
}
